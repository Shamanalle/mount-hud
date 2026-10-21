package com.kasper.mounthud;

import com.kasper.mounthud.config.HeartsPosition;
import com.kasper.mounthud.config.JumpBarMode;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Every language file has the same keys as en_us with the same placeholders, every key used in the sources of any
 * module exists, and the languages the README promises are all there.
 */
class LangConsistencyTest {
    private static final Path ROOT = Paths.get(System.getProperty("mounthud.root", "..")).toAbsolutePath().normalize();
    private static final Path LANG = ROOT.resolve("common/src/main/resources/assets/mounthud/lang");
    private static final Set<String> REQUIRED = new TreeSet<>(List.of(
            "en_us", "ru_ru", "uk_ua", "de_de", "es_es", "pt_br", "zh_cn", "fr_fr", "it_it", "pl_pl", "ja_jp", "ko_kr", "tr_tr"));
    /** A key in a Java string: "mounthud.something" (a key that ends with '.' is a prefix built on in code). */
    private static final Pattern KEY_IN_CODE = Pattern.compile("\"(mounthud\\.[a-z0-9_.]+[a-z0-9_])\"");
    private static final Pattern PLACEHOLDER = Pattern.compile("%(\\d+\\$)?[sd]");
    private static final Pattern ENTRY = Pattern.compile("\"((?:[^\"\\\\]|\\\\.)*)\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");

    private static Map<String, String> read(Path file) throws IOException {
        String text = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        Map<String, String> map = new TreeMap<>();
        Matcher m = ENTRY.matcher(text);
        while (m.find()) {
            if (map.put(m.group(1), m.group(2)) != null) {
                fail(file.getFileName() + ": key " + m.group(1) + " appears twice");
            }
        }
        return map;
    }

    private static Map<String, Map<String, String>> languages() throws IOException {
        Map<String, Map<String, String>> all = new TreeMap<>();
        try (Stream<Path> files = Files.list(LANG)) {
            for (Path f : files.filter(p -> p.toString().endsWith(".json")).collect(Collectors.toList())) {
                all.put(f.getFileName().toString().replace(".json", ""), read(f));
            }
        }
        return all;
    }

    private static List<String> placeholders(String value) {
        List<String> out = new ArrayList<>();
        Matcher m = PLACEHOLDER.matcher(value);
        while (m.find()) {
            out.add(m.group());
        }
        out.sort(null);
        return out;
    }

    @Test
    void everyLanguageIsThere() throws IOException {
        assertTrue(languages().keySet().containsAll(REQUIRED), "missing: " + REQUIRED + " vs " + languages().keySet());
    }

    @Test
    void languagesHaveTheSameKeysAndPlaceholders() throws IOException {
        Map<String, Map<String, String>> all = languages();
        Map<String, String> english = all.get("en_us");
        for (Map.Entry<String, Map<String, String>> lang : all.entrySet()) {
            assertEquals(english.keySet(), lang.getValue().keySet(), lang.getKey() + " has other keys than en_us");
            for (Map.Entry<String, String> e : lang.getValue().entrySet()) {
                assertTrue(!e.getValue().trim().isEmpty(), lang.getKey() + ": " + e.getKey() + " is empty");
                assertEquals(placeholders(english.get(e.getKey())), placeholders(e.getValue()),
                        lang.getKey() + ": placeholders of " + e.getKey());
            }
        }
    }

    @Test
    void everyKeyUsedInCodeExists() throws IOException {
        Set<String> english = languages().get("en_us").keySet();
        Set<String> used = new TreeSet<>();
        for (String module : new String[]{"common", "mc", "fabric", "forge", "neoforge"}) {
            Path src = ROOT.resolve(module).resolve("src");
            if (!Files.isDirectory(src)) {
                continue;
            }
            try (Stream<Path> files = Files.walk(src)) {
                for (Path f : files.filter(p -> p.toString().endsWith(".java") && !p.toString().contains("/test/"))
                        .collect(Collectors.toList())) {
                    Matcher m = KEY_IN_CODE.matcher(new String(Files.readAllBytes(f), StandardCharsets.UTF_8));
                    while (m.find()) {
                        used.add(m.group(1));
                    }
                }
            }
        }
        // Keys built from enum names
        for (JumpBarMode mode : JumpBarMode.values()) {
            used.add(mode.key());
        }
        for (HeartsPosition position : HeartsPosition.values()) {
            used.add(position.key());
        }
        Set<String> missing = new TreeSet<>(used);
        missing.removeAll(english);
        assertTrue(missing.isEmpty(), "keys used in code but missing in en_us: " + missing);
    }
}
