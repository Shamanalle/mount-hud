package com.kasper.mounthud.stats;

import java.util.Locale;

/**
 * A mount's health, movement speed in blocks per second and jump height in blocks, calculated from its entity
 * attributes without depending on Minecraft classes.
 */
public final class MountStats {
    /**
     * Conversion factor from a ridden land mount's {@code minecraft:movement_speed} attribute to horizontal speed in
     * blocks per second on normal ground (block friction 0.6, drag 0.546, 20 ticks per second).
     */
    public static final double SPEED_TO_BLOCKS_PER_SECOND = 43.17;
    /** Downward acceleration per tick applied to a jumping living entity in vanilla physics. */
    public static final double GRAVITY = 0.08;
    /** Vertical velocity multiplier applied in both {@code LivingEntity.travel} and {@code LivingEntity.aiStep}. */
    public static final double AIR_DRAG = 0.98;

    public final float health;
    public final float maxHealth;
    public final double blocksPerSecond;
    public final double jumpHeight;

    public MountStats(float health, float maxHealth, double movementSpeed, double jumpStrength) {
        this.health = health > 0 && !Float.isNaN(health) ? health : 0;
        this.maxHealth = maxHealth > 0 && !Float.isNaN(maxHealth) ? maxHealth : 0;
        this.blocksPerSecond = blocksPerSecond(movementSpeed);
        this.jumpHeight = jumpHeight(jumpStrength);
    }

    /** Horizontal speed in blocks per second from {@code minecraft:movement_speed}. */
    public static double blocksPerSecond(double movementSpeed) {
        if (movementSpeed <= 0 || Double.isNaN(movementSpeed) || Double.isInfinite(movementSpeed)) {
            return 0;
        }
        return movementSpeed * SPEED_TO_BLOCKS_PER_SECOND;
    }

    /**
     * Peak height in blocks reached by a full-charge jump with initial upward velocity {@code jumpStrength}, stepping
     * through vanilla's per-tick upward motion ({@code travel} moves by {@code vy} and applies {@code (vy - 0.08) * 0.98},
     * and the next tick's {@code aiStep} multiplies {@code vy} by {@code 0.98} before the next move).
     */
    public static double jumpHeight(double jumpStrength) {
        if (jumpStrength <= 0 || Double.isNaN(jumpStrength) || Double.isInfinite(jumpStrength)) {
            return 0;
        }
        double height = 0;
        double velocity = jumpStrength;
        while (velocity > 0) {
            height += velocity;
            velocity = (velocity - GRAVITY) * AIR_DRAG * AIR_DRAG;
        }
        return height;
    }

    /** Health points (half-hearts) expressed in hearts. */
    public static double hearts(double health) {
        if (health <= 0 || Double.isNaN(health) || Double.isInfinite(health)) {
            return 0;
        }
        return health / 2.0;
    }

    /** Whether the mount has a jump attribute to display. */
    public boolean hasJump() {
        return jumpHeight > 0;
    }

    /** Health as {@code "23/30"} when hurt or {@code "30"} when at full health. */
    public String healthText() {
        int max = Math.max(0, Math.round(maxHealth));
        int current = Math.max(0, Math.min(max, Math.round(health)));
        return current < max ? current + "/" + max : Integer.toString(max);
    }

    /** Speed in blocks per second rounded to one decimal place (for example {@code "9.7"}). */
    public String speedText() {
        return formatOneDecimal(blocksPerSecond);
    }

    /** Jump height in blocks rounded to one decimal place (for example {@code "2.9"}). */
    public String jumpText() {
        return formatOneDecimal(jumpHeight);
    }

    public static String formatOneDecimal(double value) {
        if (value <= 0 || Double.isNaN(value) || Double.isInfinite(value)) {
            return "0.0";
        }
        return String.format(Locale.ROOT, "%.1f", value);
    }
}
