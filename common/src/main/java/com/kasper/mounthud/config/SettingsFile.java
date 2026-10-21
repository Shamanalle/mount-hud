package com.kasper.mounthud.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

/**
 * config/mounthud.properties: one line per setting with a comment above it. A missing file, a missing line or a
 * value that cannot be read gives the default of that setting, so a broken file never stops the game.
 */
public final class SettingsFile {
    private final Path path;

    public SettingsFile(Path path) {
        this.path = path;
    }

    public Path path() {
        return path;
    }

    public Settings load() {
        Settings s = new Settings();
        if (!Files.isRegularFile(path)) {
            return s;
        }
        Properties p = new Properties();
        try (Reader in = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            p.load(in);
        } catch (IOException | IllegalArgumentException e) {
            return s;
        }
        s.enabled = bool(p, "enabled", s.enabled);
        s.hungerWhileRiding = bool(p, "hunger_while_riding", s.hungerWhileRiding);
        s.heartsPosition = choice(p, "hearts_position", HeartsPosition.class, s.heartsPosition);
        s.jumpBar = choice(p, "jump_bar", JumpBarMode.class, s.jumpBar);
        return s;
    }

    public void save(Settings s) throws IOException {
        StringBuilder out = new StringBuilder();
        out.append("# Mount HUD settings. The settings screen writes this file too (Mod Menu on Fabric, Mods list on Forge and NeoForge).\n");
        line(out, "Everything the mod changes on the HUD: true or false (false gives the vanilla HUD)", "enabled", s.enabled);
        line(out, "Hunger stays visible while riding a horse, pig, camel, strider...: true or false", "hunger_while_riding", s.hungerWhileRiding);
        line(out, "Where the mount's hearts go while hunger is shown: above_hunger or left", "hearts_position", lower(s.heartsPosition));
        line(out, "Bar above the hotbar while riding a mount that jumps: auto (experience, the jump bar while charging), always_jump or always_experience", "jump_bar", lower(s.jumpBar));
        Path dir = path.getParent();
        if (dir != null) {
            Files.createDirectories(dir);
        }
        Path tmp = path.resolveSibling(path.getFileName() + ".tmp");
        try (Writer w = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
            w.write(out.toString());
        }
        try {
            Files.move(tmp, path, java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            Files.move(tmp, path, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void line(StringBuilder out, String comment, String key, Object value) {
        out.append('\n').append("# ").append(comment).append('\n').append(key).append('=').append(value).append('\n');
    }

    private static String lower(Enum<?> e) {
        return e.name().toLowerCase(Locale.ROOT);
    }

    private static boolean bool(Properties p, String key, boolean fallback) {
        String v = p.getProperty(key);
        if (v == null) {
            return fallback;
        }
        v = v.trim();
        if (v.equalsIgnoreCase("true") || v.equalsIgnoreCase("on")) {
            return true;
        }
        if (v.equalsIgnoreCase("false") || v.equalsIgnoreCase("off")) {
            return false;
        }
        return fallback;
    }

    private static <E extends Enum<E>> E choice(Properties p, String key, Class<E> type, E fallback) {
        String v = p.getProperty(key);
        if (v == null) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, v.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}
