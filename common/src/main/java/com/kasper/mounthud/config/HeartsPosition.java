package com.kasper.mounthud.config;

/** Where the mount's hearts go while hunger is shown too. */
public enum HeartsPosition {
    /** On the right, one row above hunger; the air bubbles move up above them. */
    ABOVE_HUNGER,
    /** On the left, above your own hearts and armor. */
    LEFT;

    public String key() {
        return "mounthud.option.hearts_position." + name().toLowerCase(java.util.Locale.ROOT);
    }
}
