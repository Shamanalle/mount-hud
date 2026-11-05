package com.kasper.mounthud.client;

import com.kasper.mounthud.MountHud;
import com.kasper.mounthud.compat.Canvas;
import com.kasper.mounthud.hud.HudLayout;
import com.kasper.mounthud.hud.HudPlan;
import com.kasper.mounthud.hud.HudPlanner;
import com.kasper.mounthud.hud.HudState;
import com.kasper.mounthud.hud.MountHearts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PlayerRideableJumping;

/** The HUD while riding: reads the game for {@link HudPlanner} and draws what vanilla does not. */
public final class RidingHud {
    private RidingHud() {
    }

    /** What the HUD shows this frame. */
    public static HudPlan plan() {
        return HudPlanner.plan(MountHud.settings(), state());
    }

    /** The mod draws the mount's hearts itself (vanilla is told the mount has none, so it also draws hunger). */
    public static boolean drawsMountHearts(HudPlan plan) {
        return plan.hearts == HudPlan.Hearts.ABOVE_HUNGER || plan.hearts == HudPlan.Hearts.LEFT;
    }

    /** The jump bar is hidden although the player rides a mount that jumps: vanilla is told there is none. */
    public static boolean hidesJumpBar(HudPlan plan) {
        return plan.bar != HudPlan.Bar.JUMP;
    }

    /** The living mount whose hearts are shown, or null. */
    public static LivingEntity mount() {
        LocalPlayer player = Minecraft.getInstance().player;
        Entity vehicle = player == null ? null : player.getVehicle();
        return vehicle instanceof LivingEntity && vehicle.showVehicleHealth() ? (LivingEntity) vehicle : null;
    }

    /**
     * Draws the mount's hearts where the plan puts them, for loaders that do not count rows (Fabric).
     * {@code width} and {@code height} are the GUI's.
     */
    public static void drawMountHearts(Canvas canvas, HudPlan plan) {
        LivingEntity mount = mount();
        LocalPlayer player = Minecraft.getInstance().player;
        if (mount == null || player == null) {
            return;
        }
        int height = canvas.height();
        if (plan.hearts == HudPlan.Hearts.LEFT) {
            int y = HudLayout.leftFreeRow(height, Math.max(player.getMaxHealth(), (float) Math.ceil(player.getHealth())),
                    player.getAbsorptionAmount(), player.getArmorValue());
            drawMountHearts(canvas, mount, canvas.width() / 2 - 91, y, true);
        } else {
            drawMountHearts(canvas, mount, canvas.width() / 2 + 91, HudLayout.aboveHungerRow(height), false);
        }
    }

    /**
     * Draws the mount's hearts in rows going up from {@code y}. From the left they start at {@code x} and fill
     * rightwards as the player's hearts do; from the right they end at {@code x} and fill leftwards as hunger does.
     */
    public static void drawMountHearts(Canvas canvas, LivingEntity mount, int x, int y, boolean fromLeft) {
        int hearts = MountHearts.hearts(mount.getMaxHealth());
        float health = mount.getHealth();
        for (int i = 0; i < hearts; i++) {
            int column = i % MountHearts.PER_ROW;
            int hx = fromLeft ? x + column * MountHearts.SPACING : x - column * MountHearts.SPACING - 9;
            int hy = y - (i / MountHearts.PER_ROW) * MountHearts.ROW_HEIGHT;
            canvas.sprite(HudSprite.MOUNT_HEART_CONTAINER, hx, hy);
            MountHearts.Heart heart = MountHearts.heart(i, health);
            if (heart == MountHearts.Heart.FULL) {
                canvas.sprite(HudSprite.MOUNT_HEART_FULL, hx, hy);
            } else if (heart == MountHearts.Heart.HALF) {
                canvas.sprite(HudSprite.MOUNT_HEART_HALF, hx, hy);
            }
        }
    }

    private static HudState state() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        HudState state = new HudState();
        if (player == null || minecraft.gameMode == null) {
            return state;
        }
        state.survivalBars = minecraft.gameMode.canHurtPlayer();
        state.experience = minecraft.gameMode.hasExperience();
        LivingEntity mount = mount();
        if (mount != null) {
            state.mount(mount.getHealth(), mount.getMaxHealth());
        }
        PlayerRideableJumping jumping = player.jumpableVehicle();
        if (jumping != null) {
            state.jumping(player.getJumpRidingScale(), jumping.getJumpCooldown() > 0);
        }
        return state;
    }
}
