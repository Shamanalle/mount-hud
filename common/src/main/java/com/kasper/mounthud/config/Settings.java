package com.kasper.mounthud.config;

/** The player's settings, as in config/mounthud.properties. */
public final class Settings {
    /** Everything the mod changes on the HUD; off means the vanilla HUD. */
    public boolean enabled = true;
    /** Hunger stays visible while riding a mount with health. */
    public boolean hungerWhileRiding = true;
    /** Where the mount's hearts go while hunger is shown. */
    public HeartsPosition heartsPosition = HeartsPosition.ABOVE_HUNGER;
    /** What replaces the experience bar while riding a mount that jumps. */
    public JumpBarMode jumpBar = JumpBarMode.AUTO;

    public Settings copy() {
        Settings s = new Settings();
        s.enabled = enabled;
        s.hungerWhileRiding = hungerWhileRiding;
        s.heartsPosition = heartsPosition;
        s.jumpBar = jumpBar;
        return s;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Settings)) {
            return false;
        }
        Settings s = (Settings) o;
        return enabled == s.enabled && hungerWhileRiding == s.hungerWhileRiding
                && heartsPosition == s.heartsPosition && jumpBar == s.jumpBar;
    }

    @Override
    public int hashCode() {
        return java.util.Arrays.hashCode(new Object[]{enabled, hungerWhileRiding, heartsPosition, jumpBar});
    }
}
