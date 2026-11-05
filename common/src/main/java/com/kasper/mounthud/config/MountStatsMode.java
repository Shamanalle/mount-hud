package com.kasper.mounthud.config;

import java.util.Locale;

/** Where the mount's speed, jump height and health are shown. */
public enum MountStatsMode {
    /** In the mount's inventory screen only. */
    INVENTORY,
    /** In the mount's inventory screen and on the HUD while riding. */
    HUD_AND_INVENTORY,
    /** Never shown. */
    OFF;

    public boolean inInventory() {
        return this != OFF;
    }

    public boolean onHud() {
        return this == HUD_AND_INVENTORY;
    }

    public String key() {
        return "mounthud.option.mount_stats." + name().toLowerCase(Locale.ROOT);
    }
}
