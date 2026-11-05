package com.kasper.mounthud.hud;

/** Where the vanilla HUD puts its rows, for the loaders that do not keep count of them (Fabric). */
public final class HudLayout {
    /** The bottom row of the bars above the hotbar (hearts, hunger) is this far above the bottom of the screen. */
    public static final int BOTTOM_ROW = 39;

    private HudLayout() {
    }

    /** Rows of player hearts, health and absorption together, ten hearts to a row. */
    public static int playerHeartRows(float maxHealth, float absorption) {
        return Math.max(1, (int) Math.ceil((maxHealth + (float) Math.ceil(absorption)) / 2.0f / 10.0f));
    }

    /** How far apart the rows of player hearts are: they squeeze together once there are more than two. */
    public static int playerHeartRowHeight(int rows) {
        return Math.max(10 - Math.max(rows - 2, 0), 3);
    }

    /**
     * The y of the first free row on the left: above the player's hearts, and above armor when the player wears
     * some. {@code screenHeight} is the GUI height.
     */
    public static int leftFreeRow(int screenHeight, float maxHealth, float absorption, int armor) {
        int rows = playerHeartRows(maxHealth, absorption);
        int y = screenHeight - BOTTOM_ROW - (rows - 1) * playerHeartRowHeight(rows) - 10;
        return armor > 0 ? y - 10 : y;
    }

    /** The y of the row above hunger on the right. */
    public static int aboveHungerRow(int screenHeight) {
        return screenHeight - BOTTOM_ROW - 10;
    }
}
