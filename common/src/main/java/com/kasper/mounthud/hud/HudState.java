package com.kasper.mounthud.hud;

/** What the HUD needs to know about the player this frame, read from the game by the loader's hook. */
public final class HudState {
    /** The game mode shows health and hunger (survival, adventure). */
    public boolean survivalBars;
    /** The game mode shows experience (survival, adventure). */
    public boolean experience;
    /** Health and maximum health of the mount when it is a living one (horse, pig, camel, strider...); 0 otherwise. */
    public float mountHealth;
    public float mountMaxHealth;
    /** The mount jumps or dashes when the jump key is held (horse, donkey, mule, camel...). */
    public boolean jumpingMount;
    /** How far the jump is charged, 0 - 1. */
    public float jumpCharge;
    /** The mount cannot jump yet (a camel's dash recharging). */
    public boolean jumpCooldown;

    public boolean ridingLivingMount() {
        return mountMaxHealth > 0;
    }

    public HudState survival(boolean value) {
        survivalBars = value;
        experience = value;
        return this;
    }

    public HudState mount(float health, float maxHealth) {
        mountHealth = health;
        mountMaxHealth = maxHealth;
        return this;
    }

    public HudState jumping(float charge, boolean cooldown) {
        jumpingMount = true;
        jumpCharge = charge;
        jumpCooldown = cooldown;
        return this;
    }
}
