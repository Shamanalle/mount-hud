package com.kasper.mounthud.config;

/** What the bar above the hotbar shows while riding a mount that jumps (horse, camel, ...). */
public enum JumpBarMode {
    /** Experience; the jump bar only while a jump is charged (and while a camel's dash recharges). */
    AUTO,
    /** The jump bar all the time, as in vanilla. */
    ALWAYS_JUMP,
    /** Experience all the time: the jump bar is never shown. */
    ALWAYS_EXPERIENCE;

    public String key() {
        return "mounthud.option.jump_bar." + name().toLowerCase(java.util.Locale.ROOT);
    }
}
