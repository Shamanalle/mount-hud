package com.kasper.mounthud.hud;

/** What the HUD shows this frame while riding: decided by {@link HudPlanner}, drawn by the loader's hook. */
public final class HudPlan {
    /** Where the mount's hearts are drawn. */
    public enum Hearts {
        /** No living mount: nothing to draw. */
        NONE,
        /** In the place of hunger, as in vanilla (hunger is hidden). */
        VANILLA,
        /** On the right, one row above hunger. */
        ABOVE_HUNGER,
        /** On the left, above the player's hearts and armor. */
        LEFT
    }

    /** The bar above the hotbar. */
    public enum Bar { NONE, EXPERIENCE, JUMP }

    /** No change from vanilla: the mod is off, or the player is not riding. */
    public final boolean vanilla;
    public final boolean hunger;
    public final Hearts hearts;
    /** Rows of mount hearts. */
    public final int heartRows;
    public final Bar bar;
    /**
     * Rows the air bubbles move up from where they are without a mount (one row above hunger), so they stay above
     * mount hearts drawn on the right.
     */
    public final int airRowsUp;

    HudPlan(boolean vanilla, boolean hunger, Hearts hearts, int heartRows, Bar bar) {
        this.vanilla = vanilla;
        this.hunger = hunger;
        this.hearts = hearts;
        this.heartRows = heartRows;
        this.bar = bar;
        this.airRowsUp = airRowsUp(hearts, heartRows);
    }

    private static int airRowsUp(Hearts hearts, int heartRows) {
        switch (hearts) {
            case VANILLA:
                // The hearts take hunger's row and those above it; the bubbles sit on top
                return heartRows - 1;
            case ABOVE_HUNGER:
                return heartRows;
            default:
                return 0;
        }
    }

    /** The level number is shown with the experience bar. */
    public boolean experienceLevel() {
        return bar == Bar.EXPERIENCE;
    }

    @Override
    public String toString() {
        return "HudPlan{vanilla=" + vanilla + ", hunger=" + hunger + ", hearts=" + hearts + " x" + heartRows
                + ", bar=" + bar + ", airRowsUp=" + airRowsUp + '}';
    }
}
