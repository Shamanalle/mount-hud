package com.kasper.mounthud.hud;

/** The mount's hearts, counted the way the vanilla HUD counts them. */
public final class MountHearts {
    /** Hearts in one row. */
    public static final int PER_ROW = 10;
    /** Rows are this far apart, as the other bars. */
    public static final int ROW_HEIGHT = 10;
    /** Hearts are this far apart within a row. */
    public static final int SPACING = 8;
    /** The vanilla HUD shows at most this many hearts (three rows). */
    public static final int MAX_HEARTS = 30;

    public enum Heart { EMPTY, HALF, FULL }

    private MountHearts() {
    }

    /** Hearts shown for a mount with this maximum health: one per two health points, rounded, at most 30. */
    public static int hearts(float maxHealth) {
        if (!(maxHealth > 0)) {
            return 0;
        }
        return Math.min((int) (maxHealth + 0.5f) / 2, MAX_HEARTS);
    }

    public static int rows(int hearts) {
        return (hearts + PER_ROW - 1) / PER_ROW;
    }

    /** Whether heart number {@code index} (0 = first) is full, half or empty at this health; health is rounded up. */
    public static Heart heart(int index, float health) {
        int points = (int) Math.ceil(health);
        int left = points - index * 2;
        if (left >= 2) {
            return Heart.FULL;
        }
        return left == 1 ? Heart.HALF : Heart.EMPTY;
    }
}
