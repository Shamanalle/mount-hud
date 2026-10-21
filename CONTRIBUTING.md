# Contributing / Участие в разработке

**[English](#english)** · **[Русский](#русский)**

---

## English

### Setup

- **JDK 25** runs Gradle and compiles every Minecraft version (older ones with `--release 17` / `21`).
- Git.

```bash
git clone https://github.com/Shamanalle/mount-hud.git
cd mount-hud
./gradlew :common:test                                # fast: HUD layout, settings, translations
./gradlew build                                       # the jars of the Minecraft version in gradle.properties
./gradlew build -Pminecraft_version=1.20.1            # another version (any file in versions/)
./gradlew :fabric:runClient -Pminecraft_version=26.3  # a dev client; :neoforge / :forge likewise
```

### One build, one Minecraft version

A build is for one Minecraft version: `minecraft_version` in `gradle.properties`, or `-Pminecraft_version=…`. Its file in `versions/` names the loaders built for it (`loaders = fabric, neoforge`), their versions, the Java version and the source overlays (`compat`). CI builds every file in `versions/` in its own job.

Every version is compiled against its own Minecraft, so a call that does not exist there fails the build instead of the game. Versions whose jars come out the same are released as one jar (`versions/shared-jars.txt`), and the release puts those jars together into a few files per loader.

### Where code lives

| Directory | Compiled into | Contains |
|---|---|---|
| `common/` | every jar (plain Java 8, no Minecraft classes) | settings and their file, what the HUD shows while riding and where (`HudPlanner`), mount hearts; **translations and icon** (`src/main/resources`). Its unit tests run here |
| `mc/` | every jar | Minecraft code shared by the loaders: drawing, the settings screen, reading the mount. `src/main/java` is the same on every version; `src/compat/<name>/java` are the overlays |
| `fabric/` | the Fabric / Quilt jar | entrypoint, Mod Menu, the mixins into the vanilla HUD, `fabric.mod.json` |
| `forge/` | the Forge jar | entrypoint and Forge's HUD events, `mods.toml`. Forge 1.20.1 runs with Forge's own names: ModDevGradle's `legacyforge` builds against Mojang's names and `reobfJar` renames the jar |
| `neoforge/` | the NeoForge jar | entrypoint and NeoForge's HUD layers, `neoforge.mods.toml` |
| `versions/` | — | one file per Minecraft version; `shared-jars.txt` |

**Overlays.** `compat` in a version's file lists folders in order, for example `base,until_1_21_5`. For each name, `mc/src/compat/<name>/java` and then `<loader>/src/compat/<name>/java` (and `…/resources`) are layered over each other; a later file replaces an earlier one with the same path. `base` holds the usual version of a version-specific file, `until_X` / `since_X` / `only_X` the ones for older or newer Minecraft. A file is either in `src/main/java` or in overlays, never both.

Rules of thumb:
- Logic that does not need Minecraft goes into `common` and gets a unit test. The hooks only read the game into `HudState`, ask `HudPlanner` for a `HudPlan` and carry it out.
- `common` stays Java 8 (no `var`, records, switch expressions or `List.of` in main code): the same classes run on Minecraft 1.16.5.
- Forge and NeoForge are hooked through their own HUD events (overlays, layers): the loader already splits the vanilla HUD into pieces and keeps the row heights (`leftHeight`, `rightHeight`) other mods rely on. Fabric has no such split before 1.21.6, so there the mod changes the vanilla HUD with mixins, as small as possible (`@ModifyExpressionValue`, `@WrapOperation` from MixinExtras, which compose with other mods' mixins; no `@Overwrite`, no `@Redirect`).
- Drawing goes through the mod's own drawing class in `mc/`, not straight through Minecraft's: its API changed in 1.20 (`GuiGraphics`), 1.20.2 (sprites), 1.21.2, 1.21.6 (`drawString`, render pipelines) and again in 26.x.
- Mount stats are computed in `common` from attribute values the hook reads, so the formulas are tested.

### Translations

Language files are in `common/src/main/resources/assets/mounthud/lang/` (13 languages). Add every new key to **all** files. `LangConsistencyTest` fails if a key used in code is missing, if the languages have different keys, or if their placeholders differ.

### Documentation

The README is two files: `README.md` in English and `README.ru.md` in Russian. They have the same sections and are changed together. The rest of the documentation (CHANGELOG, this file, issue and pull request templates) is written in English first, followed by a complete Russian translation. The README is a reference for players: what the mod does, how to install it, which file to take, compatibility and every setting, without internals.

The repository description and topics live in `.github/about.json`. The *Repository about* workflow validates the file in pull requests and applies it when it changes on `main`. Applying needs the `REPO_ADMIN_TOKEN` secret (see below).

### Secrets

Set once in *Settings → Secrets and variables → Actions → New repository secret*. Tokens never go into files, commits or chats. A workflow whose token or project id is missing skips that step with a warning and stays green.

| Secret | Where to create it | Permissions |
|---|---|---|
| `MODRINTH_TOKEN` | Modrinth → *Settings → Personal access tokens* | *Create versions*, *Read projects*, *Write versions* (lets *Sync release notes* update the changelog of published versions), *Write projects* (lets *Modrinth page* upload the description, summary and gallery) |
| `CURSEFORGE_TOKEN` | CurseForge → *Account → API tokens* | an upload token (CurseForge has no scopes) |
| `REPO_ADMIN_TOKEN` | GitHub → *Settings → Developer settings → Personal access tokens → Fine-grained tokens* | *Repository access*: only this repository; *Repository permissions → Administration*: *Read and write*. Nothing else |

The Modrinth and CurseForge project ids go into `.github/publish.json` once the projects exist (Modrinth: *Project ID* from the project's menu; CurseForge: the *Project ID* number on the project page).

### Pull requests

1. Branch off `main` (`feature/…`, `fix/…`).
2. Run `./gradlew :common:test` and build the versions your change touches.
3. Describe what changed and how you tested it in game (Minecraft and loader versions, which mounts).
4. Add an entry to `CHANGELOG.md` under an *Unreleased* section, in English and in Russian. The changelog becomes the release notes on Modrinth and CurseForge: short plain bullets under Added / Changed / Fixed about what players notice, no internals.

---

## Русский

### Подготовка

- **JDK 25** запускает Gradle и компилирует все версии Minecraft (старые — с `--release 17` / `21`).
- Git.

```bash
git clone https://github.com/Shamanalle/mount-hud.git
cd mount-hud
./gradlew :common:test                                # быстро: раскладка HUD, настройки, переводы
./gradlew build                                       # JAR для версии Minecraft из gradle.properties
./gradlew build -Pminecraft_version=1.20.1            # другая версия (любой файл из versions/)
./gradlew :fabric:runClient -Pminecraft_version=26.3  # клиент для разработки; так же :neoforge / :forge
```

### Одна сборка — одна версия Minecraft

Сборка делается для одной версии Minecraft: `minecraft_version` в `gradle.properties` или `-Pminecraft_version=…`. Её файл в `versions/` называет загрузчики, которые для неё собираются (`loaders = fabric, neoforge`), их версии, версию Java и слои исходников (`compat`). CI собирает каждый файл из `versions/` в отдельном задании.

Каждая версия компилируется против своего Minecraft, поэтому вызов, которого там нет, ломает сборку, а не игру. Версии, у которых JAR получается одинаковым, выходят одним JAR (`versions/shared-jars.txt`), а релиз собирает такие JAR в несколько файлов на загрузчик.

### Где какой код

| Папка | Куда попадает | Что внутри |
|---|---|---|
| `common/` | во все JAR (обычная Java 8, без классов Minecraft) | настройки и их файл, что HUD показывает верхом и где (`HudPlanner`), сердца животного; **переводы и иконка** (`src/main/resources`). Здесь же юнит-тесты |
| `mc/` | во все JAR | код Minecraft, общий для загрузчиков: рисование, экран настроек, чтение данных животного. `src/main/java` одинаков на всех версиях; `src/compat/<имя>/java` — слои |
| `fabric/` | JAR для Fabric / Quilt | точка входа, Mod Menu, миксины в ванильный HUD, `fabric.mod.json` |
| `forge/` | JAR для Forge | точка входа и события HUD Forge, `mods.toml`. Forge 1.20.1 работает со своими именами: плагин `legacyforge` из ModDevGradle собирает против имён Mojang, а `reobfJar` переименовывает JAR |
| `neoforge/` | JAR для NeoForge | точка входа и слои HUD NeoForge, `neoforge.mods.toml` |
| `versions/` | — | по файлу на версию Minecraft; `shared-jars.txt` |

**Слои.** `compat` в файле версии перечисляет папки по порядку, например `base,until_1_21_5`. Для каждого имени накладываются `mc/src/compat/<имя>/java`, затем `<загрузчик>/src/compat/<имя>/java` (и `…/resources`); более поздний файл заменяет более ранний с тем же путём. В `base` лежит обычный вариант файла, зависящего от версии, в `until_X` / `since_X` / `only_X` — варианты для старых или новых Minecraft. Файл лежит либо в `src/main/java`, либо в слоях, но не там и там.

Правила:
- Логика, которой не нужен Minecraft, живёт в `common` и покрывается юнит-тестом. Хуки только читают игру в `HudState`, спрашивают у `HudPlanner` план (`HudPlan`) и выполняют его.
- `common` остаётся на Java 8 (в основном коде нет `var`, record, switch-выражений и `List.of`): те же классы работают на Minecraft 1.16.5.
- На Forge и NeoForge мод подключается через их собственные события HUD (overlays, layers): загрузчик уже делит ванильный HUD на части и ведёт высоты рядов (`leftHeight`, `rightHeight`), на которые опираются другие моды. У Fabric такого деления до 1.21.6 нет, поэтому там мод меняет ванильный HUD миксинами, как можно точечнее (`@ModifyExpressionValue`, `@WrapOperation` из MixinExtras, которые уживаются с миксинами других модов; без `@Overwrite` и `@Redirect`).
- Рисование идёт через свой класс в `mc/`, а не напрямую через Minecraft: его API менялся в 1.20 (`GuiGraphics`), 1.20.2 (спрайты), 1.21.2, 1.21.6 (`drawString`, render pipelines) и снова в 26.x.
- Характеристики животного считаются в `common` из значений атрибутов, которые читает хук, поэтому формулы покрыты тестами.

### Переводы

Файлы языков лежат в `common/src/main/resources/assets/mounthud/lang/` (13 языков). Каждый новый ключ добавляйте во **все** файлы. `LangConsistencyTest` упадёт, если в коде есть ключ без перевода, если наборы ключей в языках различаются или если не совпадают плейсхолдеры.

### Документация

README — это два файла: `README.md` на английском и `README.ru.md` на русском. В них одинаковые разделы, и меняются они вместе. Остальная документация (CHANGELOG, этот файл, шаблоны issue и pull request) пишется сначала на английском, затем идёт полный перевод на русский. README — справочник для игроков: что делает мод, как его поставить, какой файл взять, совместимость и все настройки, без внутреннего устройства.

Описание и темы (topics) репозитория лежат в `.github/about.json`. Workflow *Repository about* проверяет файл в pull request и применяет его, когда он меняется в `main`. Для применения нужен секрет `REPO_ADMIN_TOKEN` (см. ниже).

### Секреты

Заводятся один раз в *Settings → Secrets and variables → Actions → New repository secret*. Токены никогда не попадают в файлы, коммиты и чаты. Если токена или ID проекта нет, workflow пропускает этот шаг с предупреждением и остаётся зелёным.

| Секрет | Где создать | Права |
|---|---|---|
| `MODRINTH_TOKEN` | Modrinth → *Settings → Personal access tokens* | *Create versions*, *Read projects*, *Write versions* (чтобы *Sync release notes* обновлял описание вышедших версий), *Write projects* (чтобы *Modrinth page* выкладывал описание, summary и галерею) |
| `CURSEFORGE_TOKEN` | CurseForge → *Account → API tokens* | токен для загрузки (прав у CurseForge нет) |
| `REPO_ADMIN_TOKEN` | GitHub → *Settings → Developer settings → Personal access tokens → Fine-grained tokens* | *Repository access*: только этот репозиторий; *Repository permissions → Administration*: *Read and write*. Больше ничего |

ID проектов Modrinth и CurseForge вписываются в `.github/publish.json`, когда проекты созданы (Modrinth — *Project ID* из меню проекта; CurseForge — число *Project ID* на странице проекта).

### Pull request

1. Создайте ветку от `main` (`feature/…`, `fix/…`).
2. Запустите `./gradlew :common:test` и соберите версии, которых касается изменение.
3. Опишите, что изменилось и как вы проверяли в игре (версии Minecraft и загрузчика, на каких животных).
4. Добавьте запись в `CHANGELOG.md` в раздел *Unreleased* на английском и на русском. Журнал изменений становится описанием версии на Modrinth и CurseForge: короткие простые пункты в разделах «Добавлено / Изменено / Исправлено» о том, что заметят игроки, без внутреннего устройства.
