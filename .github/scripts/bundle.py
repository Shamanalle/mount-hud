"""Puts the release jars together into the multi-version jars of versions/bundles.txt.

Each release jar (one per loader and group of versions/shared-jars.txt, picked by release_groups.py) becomes a
variant of a bundle: its classes move from com.kasper.mounthud to com.kasper.mounthud.v.<variant>, its mixins get
a config of their own, and the small bootstrap in boot/ starts the variant made for the running loader and
Minecraft version. Every variant was compiled, remapped and tested against its own Minecraft, so the bundle holds
exactly the code of the jars it replaces.

Usage:
  bundle.py <release jar folder> <output folder>
      Builds the jars of versions/bundles.txt from the release jars (release_groups.py check <jars> <folder>).
  bundle.py entries
      Prints the store uploads as JSON, one per bundle and loader, for the upload matrix of publish.yml.
  bundle.py games
      Prints the game starts as JSON, one per loader and Minecraft version of every bundle, for the start-up
      check of the bundled jars.
  bundle.py verify <output folder>
      Checks that every bundled jar in <output folder> has valid class headers, bootstrap classes, variants
      mapping, relocated entrypoints and mixin classes.
"""
import json
import re
import struct
import subprocess
import sys
import tempfile
import zipfile
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import release_groups  # noqa: E402

ROOT = release_groups.ROOT
BOOT = ROOT / 'boot' / 'src'
BOOT_PACKAGE = 'com/kasper/mounthud/boot/'
PREFIX = b'com/kasper/mounthud/'
RELOCATED = re.compile(rb'com/kasper/mounthud/')
MOD_ANNOTATIONS = {b'Lnet/minecraftforge/fml/common/Mod;', b'Lnet/neoforged/fml/common/Mod;'}
# Unknown annotation types are ignored, so the variants' entry classes lose @Mod and FML only sees the bootstrap.
MOD_REPLACEMENT = b'Lcom/kasper/mounthud/boot/VariantEntry;'
CONFIGS = {'mounthud.mixins.json'}
METADATA = release_groups.METADATA | CONFIGS
# Fixed entry time, so the same inputs give the same jar.
TIME = (2020, 1, 1, 0, 0, 0)
# The bundled Forge jar for 1.20.1 runs on NeoForged's 1.20.1 too and is published under the neoforge loader too.
ALSO_PUBLISHED = {('forge', '1.20.1'): 'neoforge'}
# Quilt runs Fabric mods, so the stores list the Fabric jars for it too.
STORE_LOADERS = {'fabric': ['fabric', 'quilt']}
QUILT_LOADER = '0.30.1'

BOOT_CLASSES = {
    'fabric': ['FabricBoot', 'ModMenuBoot'],
    'forge': ['ForgeBoot'],
    'neoforge': ['NeoForgeBoot'],
}


def bundles():
    """(loaders, oldest version, newest version) of every line of versions/bundles.txt."""
    result = []
    for line in (release_groups.VERSIONS / 'bundles.txt').read_text().splitlines():
        line = line.strip()
        if not line or line.startswith('#'):
            continue
        loaders, versions = (part.strip() for part in line.split(':', 1))
        oldest, newest = versions.split()
        result.append((loaders.split(), oldest, newest))
    return result


def within(version, oldest, newest):
    key = release_groups.version_key
    return key(oldest) <= key(version) <= key(newest)


def variants_of(loaders, oldest, newest):
    """(loader, versions) of the release jars a bundle is made of."""
    entries = release_groups.release_entries()
    return [(loader, versions) for loader in loaders for entry_loader, versions in entries
            if entry_loader == loader and within(versions[0], oldest, newest) and within(versions[-1], oldest, newest)]


def check_coverage():
    """Every release jar goes into exactly one bundle."""
    covered = {}
    for loaders, oldest, newest in bundles():
        for loader, versions in variants_of(loaders, oldest, newest):
            covered.setdefault((loader, tuple(versions)), []).append(f'{" ".join(loaders)} {oldest}-{newest}')
    problems = []
    for loader, versions in release_groups.release_entries():
        found = covered.get((loader, tuple(versions)), [])
        if len(found) != 1:
            problems.append(f'{loader} {release_groups.label(versions)} is in {len(found)} bundles')
    if problems:
        sys.exit('versions/bundles.txt: ' + '; '.join(problems))


def jar_name(mod_version, loaders, oldest, newest):
    loader = f'{loaders[0]}-' if len(loaders) == 1 else ''
    return f'mount-hud-{loader}{mod_version}+mc{oldest}-{newest}.jar'


def variant_id(loader, versions):
    return f'{loader}_{versions[-1].replace(".", "_")}'


def relocate_class(data, variant, max_major=None):
    """Moves the mod's classes into the variant's package by rewriting the names in the constant pool."""
    target = f'com/kasper/mounthud/v/{variant}/'.encode()
    if data[:4] != b'\xca\xfe\xba\xbe':
        raise ValueError('not a class file')
    count = struct.unpack('>H', data[8:10])[0]
    out = bytearray(data[:10])
    if max_major is not None and struct.unpack('>H', data[6:8])[0] > max_major:
        out[6:8] = struct.pack('>H', max_major)
    pos = 10
    index = 1
    sizes = {3: 4, 4: 4, 5: 8, 6: 8, 7: 2, 8: 2, 9: 4, 10: 4, 11: 4, 12: 4, 15: 3, 16: 2, 17: 4, 18: 4, 19: 2, 20: 2}
    while index < count:
        tag = data[pos]
        if tag == 1:
            length = struct.unpack('>H', data[pos + 1:pos + 3])[0]
            text = data[pos + 3:pos + 3 + length]
            text = RELOCATED.sub(target, text)
            if text in MOD_ANNOTATIONS:
                text = MOD_REPLACEMENT
            out += bytes([1]) + struct.pack('>H', len(text)) + text
            pos += 3 + length
        else:
            size = sizes[tag]
            out += data[pos:pos + 1 + size]
            pos += 1 + size
        index += 2 if tag in (5, 6) else 1
    out += data[pos:]
    return bytes(out)


def relocate_name(name, variant):
    prefix = 'com/kasper/mounthud/'
    if name.startswith(prefix):
        return f'com/kasper/mounthud/v/{variant}/{name[len(prefix):]}'
    return None


def compile_boot(folder):
    sources = sorted(str(path) for path in (BOOT / 'main' / 'java').rglob('*.java'))
    subprocess.run(['javac', '--release', '8', '-implicit:none', '-d', str(folder),
                    '-sourcepath', str(BOOT / 'stubs' / 'java'), *sources], check=True)


def level_number(level):
    return int(level.split('_')[1])


def toml_with(text, minecraft_range, mixins, loader_range=None):
    """The mod metadata of a variant, changed to the bundle's Minecraft range and mixin configs."""
    text = re.sub(r'\n\[\[mixins\]\]\nconfig = "[^"]*"\n', '\n', text)
    blocks = ''.join(f'[[mixins]]\nconfig = "{config}"\n\n' for config in mixins)
    text = text.replace('\n[[dependencies.mounthud]]', '\n' + blocks + '[[dependencies.mounthud]]', 1) \
        if blocks else text
    text = re.sub(r'(modId = "minecraft"\n(?:[^\[\n].*\n)*?versionRange = )"[^"]*"',
                  lambda match: f'{match.group(1)}"{minecraft_range}"', text)
    if loader_range:
        text = re.sub(r'^loaderVersion = "[^"]*"', f'loaderVersion = "{loader_range}"', text, flags=re.M)
    return re.sub(r'\n{3,}', '\n\n', text)


def loader_major(text):
    match = re.search(r'^loaderVersion = "\[(\d+),', text, flags=re.M)
    return int(match.group(1)) if match else None


def build(folder, output, loaders, oldest, newest, boot):
    parts = variants_of(loaders, oldest, newest)
    max_major = (44 + int(release_groups.properties(oldest)['java_version'])) \
        if ('forge' in loaders or 'neoforge' in loaders) else None
    entries = {}  # name -> bytes
    shared = {}   # resource name -> (bytes, variant)
    nested = {}
    properties = []
    configs = {loader: [] for loader in loaders}
    levels = []
    metadata = []  # (loader, file name, text, oldest version of the variant)
    mod_ver = mod_version()
    for loader, versions in parts:
        path = release_groups.jar(folder, loader, release_groups.label(versions))
        variant = variant_id(loader, versions)
        for version in versions:
            properties.append(f'{loader}.{version}={variant}')
        mixins = []
        with zipfile.ZipFile(path) as archive:
            for name in archive.namelist():
                if name.endswith('/'):
                    continue
                data = archive.read(name)
                moved = relocate_name(name, variant)
                if moved:
                    if not name.endswith('.class'):
                        sys.exit(f'{path.name}: unexpected resource {name} in the class packages')
                    entries[moved] = relocate_class(data, variant, max_major)
                elif name in CONFIGS:
                    config = json.loads(data)
                    if config.get('mixins') or config.get('server'):
                        sys.exit(f'{path.name}: only client mixins can be bundled')
                    if not config['package'].startswith('com.kasper.mounthud.'):
                        sys.exit(f'{path.name}: {name} has an unexpected package')
                    subpackage = config['package'][len('com.kasper.mounthud.'):]
                    mixins += config.get('client', [])
                    levels.append(config['compatibilityLevel'])
                    config['package'] = f'com.kasper.mounthud.v.{variant}.{subpackage}'
                    config['plugin'] = 'com.kasper.mounthud.boot.VariantMixinPlugin'
                    config['client'] = []
                    config_name = f'mounthud-{variant}.mixins.json'
                    entries[config_name] = config
                    configs[loader].append(config_name)
                elif name in METADATA:
                    if name != 'META-INF/MANIFEST.MF':
                        metadata.append((loader, name, data.decode(), versions[0]))
                elif name.startswith('META-INF/jars/') or name.startswith('META-INF/jarjar/'):
                    digest = release_groups.digest(name, data)
                    if name in nested and nested[name][0] != digest:
                        sys.exit(f'{path.name}: {name} differs from the one of another variant')
                    nested.setdefault(name, (digest, data))
                else:
                    if name in shared and shared[name][0] != data:
                        sys.exit(f'{path.name}: {name} differs from the one of variant {shared[name][1]}')
                    shared.setdefault(name, (data, variant))
            properties.append(f'{variant}.mixins={",".join(mixins)}')
            entry = [name for name in entries if name.startswith(f'com/kasper/mounthud/v/{variant}/')
                     and name.endswith('.class') and MOD_REPLACEMENT in entries[name]]
            if loader == 'fabric':
                fabric = json.loads(archive.read('fabric.mod.json'))
                for key, values in fabric['entrypoints'].items():
                    properties.append(f'{variant}.{key}={relocate_entry(values[0])}')
            else:
                if len(entry) != 1:
                    sys.exit(f'{path.name}: expected one @Mod class, found {len(entry)}')
                properties.append(
                    f'{variant}.entry={entry[0][len(f"com/kasper/mounthud/v/{variant}/"):-6].replace("/", ".")}')

    # Mixin rejects compatibility levels it does not know, and it parses every config of the jar,
    # so all of them get the lowest level of the bundle.
    if levels:
        level = min(levels, key=level_number)
        for name, value in list(entries.items()):
            if isinstance(value, dict):
                value['compatibilityLevel'] = level
                entries[name] = (json.dumps(value, indent=2) + '\n').encode()

    first = parts[0][1][0]
    last = parts[-1][1][-1]
    files = {}
    manifest = ['Manifest-Version: 1.0']
    if 'fabric' in loaders:
        files['fabric.mod.json'] = fabric_metadata(metadata, configs['fabric'], loaders_versions('fabric', parts))
        manifest.append('Fabric-Loom-Mixin-Remap-Type: static')
    if 'forge' in loaders:
        tomls = texts(metadata, 'forge', 'META-INF/mods.toml')
        text = tomls[0]
        majors = [loader_major(value) for value in tomls]
        forge_versions = loaders_versions('forge', parts)
        files['META-INF/mods.toml'] = toml_with(text, f'[{forge_versions[0]},{forge_versions[-1]}]',
                                                configs['forge'], f'[{min(majors)},)')
        if configs['forge']:
            manifest.append('MixinConfigs: ' + ','.join(configs['forge']))
    if 'neoforge' in loaders:
        neo_versions = loaders_versions('neoforge', parts)
        neo_range = f'[{neo_versions[0]},{neo_versions[-1]}]'
        for name in ('META-INF/mods.toml', 'META-INF/neoforge.mods.toml'):
            found = texts(metadata, 'neoforge', name)
            if found:
                if name in files:
                    sys.exit(f'{name} is needed by both Forge and NeoForge')
                files[name] = toml_with(found[0], neo_range, configs['neoforge'])
    if 'forge' in loaders or 'neoforge' in loaders:
        files['pack.mcmeta'] = pack_metadata(loaders_versions('forge' if 'forge' in loaders else 'neoforge', parts))

    properties.sort()
    boot_classes = ['Variants', 'VariantMixinPlugin'] + [name for loader in loaders for name in BOOT_CLASSES[loader]]
    jar = Path(output) / jar_name(mod_ver, loaders, oldest, newest)
    with zipfile.ZipFile(jar, 'w', zipfile.ZIP_DEFLATED) as archive:
        def put(name, data):
            info = zipfile.ZipInfo(name, TIME)
            info.compress_type = zipfile.ZIP_DEFLATED
            archive.writestr(info, data)

        put('META-INF/MANIFEST.MF', manifest_text(manifest))
        for name in sorted(files):
            put(name, files[name])
        for name in sorted(nested):
            put(name, nested[name][1])
        for name in sorted(shared):
            put(name, shared[name][0])
        for name in boot_classes:
            put(f'{BOOT_PACKAGE}{name}.class', (Path(boot) / f'{BOOT_PACKAGE}{name}.class').read_bytes())
        put(f'{BOOT_PACKAGE}variants.properties', '\n'.join(properties) + '\n')
        for name in sorted(entries):
            put(name, entries[name])
    print(f'{jar.name}: {", ".join(variant_id(loader, versions) for loader, versions in parts)}')
    if not (within(first, oldest, newest) and first == oldest and last == newest):
        sys.exit(f'{jar.name}: the jars found cover {first} - {last} only')
    return jar


def relocate_entry(name):
    return name[len('com.kasper.mounthud.'):]


def loaders_versions(loader, parts):
    return [version for part_loader, versions in parts if part_loader == loader for version in versions]


def manifest_text(lines):
    out = []
    for line in lines:
        data = line.encode()
        out.append(data[:72])
        data = data[72:]
        while data:
            out.append(b' ' + data[:71])
            data = data[71:]
    return b'\r\n'.join(out) + b'\r\n\r\n'


def texts(metadata, loader, name):
    """The given metadata file of every variant of the loader, oldest Minecraft version first."""
    found = sorted((entry for entry in metadata if entry[:2] == (loader, name)),
                   key=lambda entry: release_groups.version_key(entry[3]))
    return [entry[2] for entry in found]


def fabric_metadata(metadata, configs, versions):
    all_metadata = [json.loads(text) for text in texts(metadata, 'fabric', 'fabric.mod.json')]
    data = all_metadata[0]
    loader_mins = [value['depends']['fabricloader'] for value in all_metadata]
    javas = [value['depends']['java'] for value in all_metadata]
    data['entrypoints'] = {
        'client': ['com.kasper.mounthud.boot.FabricBoot'],
        'modmenu': ['com.kasper.mounthud.boot.ModMenuBoot'],
    }
    data['mixins'] = configs
    depends = data['depends']
    depends['fabricloader'] = max(loader_mins, key=lambda value: release_groups.version_key(value.lstrip('>=')))
    depends['minecraft'] = f'>={versions[0]} <={versions[-1]}'
    depends['java'] = min(javas, key=lambda value: int(value.lstrip('>=')))
    return json.dumps(data, indent=2, ensure_ascii=False) + '\n'


def pack_metadata(versions):
    """pack.mcmeta for the given versions, as build.gradle writes it."""
    formats = [int(release_groups.properties(version)['resource_pack_format']) for version in versions]
    oldest, newest = min(formats), max(formats)
    fields = ['"description": "Mount HUD resources"']
    if oldest < 65:
        fields.append(f'"pack_format": {oldest}')
        if newest != oldest:
            fields.append(f'"supported_formats": [{oldest}, {newest}]')
    if newest >= 65:
        fields += [f'"min_format": {oldest}', f'"max_format": {newest}']
    return '{\n  "pack": {\n    ' + ',\n    '.join(fields) + '\n  }\n}\n'


def mod_version():
    for line in (ROOT / 'gradle.properties').read_text().splitlines():
        if line.split('=', 1)[0].strip() == 'mod_version':
            return line.split('=', 1)[1].strip()
    sys.exit('gradle.properties has no mod_version')


def entries_json():
    """One store upload per bundle and loader."""
    result = []
    for loaders, oldest, newest in bundles():
        parts = variants_of(loaders, oldest, newest)
        file = jar_name(mod_version(), loaders, oldest, newest)
        for loader in loaders:
            versions = loaders_versions(loader, parts)
            result.append({'loader': loader, 'label': release_groups.label(versions), 'versions': ' '.join(versions),
                           'file': file, 'marker': len(loaders) > 1,
                           'loaders': ' '.join(STORE_LOADERS.get(loader, [loader]))})
        for (loader, version), other in ALSO_PUBLISHED.items():
            if loader in loaders and version in loaders_versions(loader, parts):
                result.append({'loader': other, 'label': version, 'versions': version, 'file': file, 'marker': True,
                               'loaders': other})
    return json.dumps(result)


def games():
    """One game start per loader and Minecraft version a bundle is published for."""
    result = []
    for entry in json.loads(entries_json()):
        for version in entry['versions'].split():
            key = release_groups.version_key(version)
            java = 17 if key < release_groups.version_key('1.20.5') else 21 if key[0] == 1 else 25
            launch = f"{entry['loader']}:{version}"
            props = release_groups.properties(version)
            if entry['loader'] == 'forge' and props.get('forge_version'):
                launch = f"forge:{props['forge_version']}"
            if entry['loader'] == 'neoforge' and key[0] != 1:
                launch = f"neoforge:{props['neoforge_version']}"
            result.append({'loader': entry['loader'], 'mc': version, 'file': entry['file'], 'launch': launch,
                           'java': java})
            if 'quilt' in entry['loaders'].split() and version in (entry['versions'].split()[0], entry['versions'].split()[-1]):
                result.append({'loader': 'quilt', 'mc': version, 'file': entry['file'],
                               'launch': f'quilt:{version}:{QUILT_LOADER}', 'java': java})
    print(json.dumps(result))


def verify(output):
    """Checks that every bundled jar in output has its bootstrap, relocated variants and mixin classes intact."""
    check_coverage()
    folder = Path(output)
    mod_ver = mod_version()
    for loaders, oldest, newest in bundles():
        path = folder / jar_name(mod_ver, loaders, oldest, newest)
        if not path.is_file():
            sys.exit(f'missing bundled jar: {path}')
        with zipfile.ZipFile(path) as archive:
            names = set(archive.namelist())
            for req in ('META-INF/MANIFEST.MF', f'{BOOT_PACKAGE}Variants.class',
                        f'{BOOT_PACKAGE}VariantMixinPlugin.class', f'{BOOT_PACKAGE}variants.properties'):
                if req not in names:
                    sys.exit(f'{path.name}: missing {req}')
            for name in names:
                if name.endswith('.class') and archive.read(name)[:4] != b'\xca\xfe\xba\xbe':
                    sys.exit(f'{path.name}: invalid class header in {name}')
            props = {}
            for line in archive.read(f'{BOOT_PACKAGE}variants.properties').decode().splitlines():
                if '=' in line:
                    k, v = line.split('=', 1)
                    props[k] = v
            for loader, versions in variants_of(loaders, oldest, newest):
                vid = variant_id(loader, versions)
                for ver in versions:
                    if props.get(f'{loader}.{ver}') != vid:
                        sys.exit(f'{path.name}: {loader}.{ver} does not map to {vid}')
                cfg_name = f'mounthud-{vid}.mixins.json'
                if cfg_name in names:
                    pkg = json.loads(archive.read(cfg_name))['package'].replace('.', '/')
                    for mixin in filter(None, props.get(f'{vid}.mixins', '').split(',')):
                        if f'{pkg}/{mixin}.class' not in names:
                            sys.exit(f'{path.name}: missing relocated mixin {pkg}/{mixin}.class')
                for suffix in ('client', 'modmenu', 'entry'):
                    target_cls = props.get(f'{vid}.{suffix}')
                    if target_cls and f'com/kasper/mounthud/v/{vid}/{target_cls.replace(".", "/")}.class' not in names:
                        sys.exit(f'{path.name}: missing entrypoint {target_cls} for {vid}')
        print(f'verified {path.name}')


def main(folder, output):
    check_coverage()
    Path(output).mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory() as boot:
        compile_boot(boot)
        for loaders, oldest, newest in bundles():
            build(folder, output, loaders, oldest, newest, boot)


if __name__ == '__main__':
    if len(sys.argv) == 2 and sys.argv[1] == 'entries':
        print(entries_json())
    elif len(sys.argv) == 2 and sys.argv[1] == 'games':
        games()
    elif len(sys.argv) == 3 and sys.argv[1] == 'verify':
        verify(sys.argv[2])
    elif len(sys.argv) == 3:
        main(sys.argv[1], sys.argv[2])
    else:
        sys.exit(__doc__)
