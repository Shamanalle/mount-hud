package com.kasper.mounthud;

import com.kasper.mounthud.config.Settings;
import com.kasper.mounthud.config.SettingsFile;

import java.nio.file.Path;
import java.util.function.BiConsumer;

/** The mod's state shared by every loader: its id and the player's settings. */
public final class MountHud {
    public static final String MOD_ID = "mounthud";
    public static final String NAME = "Mount HUD";

    /** Where warnings go: the game's log once the loader's entrypoint has set it, standard error before. */
    private static BiConsumer<String, Throwable> warn = (message, error) -> {
        System.err.println("[" + NAME + "] " + message);
        if (error != null) {
            error.printStackTrace();
        }
    };
    private static SettingsFile file;
    private static Settings settings = new Settings();

    private MountHud() {
    }

    /** Called once by the loader's entrypoint with its config folder and its log. */
    public static synchronized void init(Path configDir, BiConsumer<String, Throwable> logWarning) {
        warn = logWarning;
        file = new SettingsFile(configDir.resolve(MOD_ID + ".properties"));
        settings = file.load();
        // Writes the file with every setting and its comment, so it can be edited by hand
        save();
    }

    public static synchronized Settings settings() {
        return settings;
    }

    /** Replaces the settings (from the settings screen) and writes them to the config file. */
    public static synchronized void apply(Settings changed) {
        settings = changed.copy();
        save();
    }

    private static void save() {
        if (file == null) {
            return;
        }
        try {
            file.save(settings);
        } catch (Exception e) {
            warn.accept("Could not write " + file.path(), e);
        }
    }
}
