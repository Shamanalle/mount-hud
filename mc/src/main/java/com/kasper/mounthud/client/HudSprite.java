package com.kasper.mounthud.client;

/** The vanilla HUD sprites the mod draws: by name since Minecraft 1.20.2, from icons.png before. */
public enum HudSprite {
    MOUNT_HEART_CONTAINER("hud/heart/vehicle_container", 52, 9),
    MOUNT_HEART_FULL("hud/heart/vehicle_full", 88, 9),
    MOUNT_HEART_HALF("hud/heart/vehicle_half", 97, 9);

    /** The sprite's name in the GUI atlas (textures/gui/sprites/...). */
    public final String path;
    /** Where it is in textures/gui/icons.png (Minecraft 1.20.1 and older). */
    public final int u;
    public final int v;

    HudSprite(String path, int u, int v) {
        this.path = path;
        this.u = u;
        this.v = v;
    }
}
