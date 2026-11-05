package com.kasper.mounthud.client;

import com.kasper.mounthud.MountHud;
import com.kasper.mounthud.compat.Canvas;
import com.kasper.mounthud.compat.Mounts;
import com.kasper.mounthud.config.Settings;
import com.kasper.mounthud.hud.HudLayout;
import com.kasper.mounthud.hud.HudPlan;
import com.kasper.mounthud.hud.HudPlanner;
import com.kasper.mounthud.hud.HudState;
import com.kasper.mounthud.hud.MountHearts;
import com.kasper.mounthud.stats.MountStats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.lang.reflect.Field;

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
        return Mounts.healthMount(Minecraft.getInstance().player);
    }

    /** Calculates the mount's health, speed in blocks per second and jump height in blocks. */
    public static MountStats stats(LivingEntity mount) {
        double speed = mount.getAttributeValue(Attributes.MOVEMENT_SPEED);
        AttributeInstance jump = mount.getAttribute(Attributes.JUMP_STRENGTH);
        double jumpStrength = jump == null ? 0 : jump.getValue();
        return new MountStats(mount.getHealth(), mount.getMaxHealth(), speed, jumpStrength);
    }

    /** Single-line summary of the mount's stats for the HUD or chested mount inventories. */
    public static String hudStatsText(MountStats stats) {
        String line = I18n.get("mounthud.stats.health", stats.healthText())
                + "  " + I18n.get("mounthud.stats.speed", stats.speedText());
        if (stats.hasJump()) {
            line += "  " + I18n.get("mounthud.stats.jump", stats.jumpText());
        }
        return line;
    }

    /**
     * Draws the mount's hearts and optional HUD stats where the plan puts them, for loaders that do not count rows
     * (Fabric).
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
            if (plan.stats) {
                drawHudStats(canvas, mount, canvas.width() / 2 - 91, y - plan.heartRows * MountHearts.ROW_HEIGHT, true);
            }
        } else if (plan.hearts == HudPlan.Hearts.ABOVE_HUNGER) {
            int y = HudLayout.aboveHungerRow(height);
            drawMountHearts(canvas, mount, canvas.width() / 2 + 91, y, false);
            if (plan.stats) {
                drawHudStats(canvas, mount, canvas.width() / 2 + 91, y - plan.heartRows * MountHearts.ROW_HEIGHT, false);
            }
        } else if (plan.stats && plan.hearts == HudPlan.Hearts.VANILLA) {
            int y = height - HudLayout.BOTTOM_ROW;
            drawHudStats(canvas, mount, canvas.width() / 2 + 91, y - plan.heartRows * MountHearts.ROW_HEIGHT, false);
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

    /** Draws the mount's stats line on the HUD at row {@code y}, left-aligned or right-aligned to {@code x}. */
    public static void drawHudStats(Canvas canvas, LivingEntity mount, int x, int y, boolean fromLeft) {
        String line = hudStatsText(stats(mount));
        int tx = fromLeft ? x : x - canvas.textWidth(line);
        canvas.text(line, tx, y, 0xFFFFFFFF, true);
    }

    /**
     * Draws the mount's health, speed and jump height inside {@link HorseInventoryScreen} (in container-relative
     * coordinates, where {@code (0, 0)} is the top-left corner of the 176x166 window).
     */
    public static void drawInventoryStats(Canvas canvas, Screen screen) {
        Settings settings = MountHud.settings();
        if (!settings.enabled || !settings.mountStats.inInventory() || !(screen instanceof HorseInventoryScreen)) {
            return;
        }
        LivingEntity mount = inventoryMount(screen);
        if (mount == null) {
            return;
        }
        MountStats stats = stats(mount);
        HorseInventoryScreen horseScreen = (HorseInventoryScreen) screen;
        if (horseScreen.getMenu().slots.size() <= 38) {
            canvas.text(I18n.get("mounthud.stats.health", stats.healthText()), 83, 22, 0xFF404040, false);
            canvas.text(I18n.get("mounthud.stats.speed", stats.speedText()), 83, 36, 0xFF404040, false);
            if (stats.hasJump()) {
                canvas.text(I18n.get("mounthud.stats.jump", stats.jumpText()), 83, 50, 0xFF404040, false);
            }
        } else {
            String line = hudStatsText(stats);
            canvas.text(line, 176 - canvas.textWidth(line), -11, 0xFFFFFFFF, true);
        }
    }

    private static LivingEntity inventoryMount(Screen screen) {
        for (Class<?> c = screen.getClass(); c != null && c != Screen.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                if (LivingEntity.class.isAssignableFrom(field.getType())) {
                    try {
                        field.setAccessible(true);
                        Object value = field.get(screen);
                        if (value instanceof LivingEntity) {
                            return (LivingEntity) value;
                        }
                    } catch (ReflectiveOperationException ignored) {
                    }
                }
            }
        }
        return mount();
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
        if (Mounts.isJumpingMount(player)) {
            state.jumping(player.getJumpRidingScale(), Mounts.isJumpOnCooldown(player));
        }
        return state;
    }
}
