package com.kasper.mounthud.compat;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/** Mount queries across Minecraft versions (1.16.5: before Entity.showVehicleHealth()). */
public final class Mounts {
    private Mounts() {
    }

    /** The living mount whose hearts are shown on the HUD, or null. */
    public static LivingEntity healthMount(LocalPlayer player) {
        Entity vehicle = player == null ? null : player.getVehicle();
        return vehicle instanceof LivingEntity ? (LivingEntity) vehicle : null;
    }

    /** Whether the player is currently riding a mount with a jump bar. */
    public static boolean isJumpingMount(LocalPlayer player) {
        return player != null && player.isRidingJumpable();
    }

    /** Whether the mount's jump or dash is on cooldown. */
    public static boolean isJumpOnCooldown(LocalPlayer player) {
        return false;
    }
}
