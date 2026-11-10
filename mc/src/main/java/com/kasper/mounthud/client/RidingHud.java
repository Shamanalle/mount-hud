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
        AttributeInstance speedAttr = mount.getAttribute(Attributes.MOVEMENT_SPEED);
        double speed = speedAttr == null ? 0 : speedAttr.getValue();
        AttributeInstance jumpAttr = mount.getAttribute(Attributes.JUMP_STRENGTH);
        double jumpStrength = jumpAttr == null ? 0 : jumpAttr.getValue();
        return new MountStats(mount.getHealth(), mount.getMaxHealth(), speed, jumpStrength);
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
        drawMountHearts(canvas, mount.getHealth(), mount.getMaxHealth(), x, y, fromLeft);
    }

    /** Draws {@code health / maxHealth} mount hearts in rows going up from {@code y}. */
    public static void drawMountHearts(Canvas canvas, float health, float maxHealth, int x, int y, boolean fromLeft) {
        int hearts = MountHearts.hearts(maxHealth);
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

    /** Draws the mount's stats badge on the HUD at row {@code y}, left-aligned or right-aligned to {@code x}. */
    public static void drawHudStats(Canvas canvas, LivingEntity mount, int x, int y, boolean fromLeft) {
        drawStatsPill(canvas, stats(mount), x, y, fromLeft);
    }

    /** Draws a compact translucent stats badge with a mount heart sprite and crisp pixel separators. */
    public static void drawStatsPill(Canvas canvas, MountStats stats, int x, int y, boolean fromLeft) {
        String health = stats.healthText();
        String speed = I18n.get("mounthud.stats.speed", stats.speedText());
        String jump = stats.hasJump() ? I18n.get("mounthud.stats.jump", stats.jumpText()) : null;

        int heartGap = 11;
        int sep = 9;
        int healthW = canvas.textWidth(health);
        int speedW = canvas.textWidth(speed);
        int jumpW = jump != null ? canvas.textWidth(jump) : 0;
        int totalW = heartGap + healthW + sep + speedW + (jump != null ? sep + jumpW : 0);
        int startX = fromLeft ? x : x - totalW;

        canvas.fill(startX - 1, y - 1, startX + totalW + 1, y + 9, 0x54000000);
        canvas.fill(startX - 2, y, startX + totalW + 2, y + 8, 0x54000000);

        canvas.sprite(HudSprite.MOUNT_HEART_CONTAINER, startX, y - 1);
        if (stats.health > 0) {
            canvas.sprite(HudSprite.MOUNT_HEART_FULL, startX, y - 1);
        }

        int cx = startX + heartGap;
        canvas.text(health, cx, y, 0xFFFFFFFF, true);
        cx += healthW;

        drawSeparatorDot(canvas, cx + 3, y + 3);
        cx += sep;
        canvas.text(speed, cx, y, 0xFFFFFFFF, true);
        cx += speedW;

        if (jump != null) {
            drawSeparatorDot(canvas, cx + 3, y + 3);
            cx += sep;
            canvas.text(jump, cx, y, 0xFFFFFFFF, true);
        }
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
            int left = 83;
            int right = 164;
            int barW = right - left;
            boolean hasJump = stats.hasJump();
            int y1 = hasJump ? 22 : 30;
            int y2 = hasJump ? 38 : 47;

            // Row 1: health sprite + value + dual-tone rating bar (max potential + current health)
            canvas.sprite(HudSprite.MOUNT_HEART_CONTAINER, left, y1 - 1);
            if (stats.health > 0) {
                canvas.sprite(HudSprite.MOUNT_HEART_FULL, left, y1 - 1);
            }
            canvas.text(stats.healthText(), left + 12, y1, 0xFF3F3F3F, false);
            int hbY = y1 + 10;
            canvas.fill(left, hbY, right, hbY + 2, 0xFF9E9E9E);
            int maxW = (int) Math.round(barW * stats.maxHealthRatio());
            if (maxW > 0) {
                canvas.fill(left, hbY, left + maxW, hbY + 2, 0xFF6E5C58);
            }
            int curW = (int) Math.round(barW * stats.healthRatio());
            if (curW > 0) {
                canvas.fill(left, hbY, left + curW, hbY + 2, 0xFFE06633);
            }

            // Row 2: speed chevron icon + blocks/s + rating bar
            drawSpeedIcon(canvas, left, y2 + 1, 0xFF236E7E);
            canvas.text(I18n.get("mounthud.stats.speed", stats.speedText()), left + 12, y2, 0xFF3F3F3F, false);
            int sbY = y2 + 10;
            canvas.fill(left, sbY, right, sbY + 2, 0xFF9E9E9E);
            int speedW = (int) Math.round(barW * stats.speedRatio());
            if (speedW > 0) {
                canvas.fill(left, sbY, left + speedW, sbY + 2, 0xFF2B8EA1);
            }

            // Row 3: jump arrow icon + blocks + rating bar
            if (hasJump) {
                int y3 = 54;
                drawJumpIcon(canvas, left, y3, 0xFF377A2B);
                canvas.text(I18n.get("mounthud.stats.jump", stats.jumpText()), left + 12, y3, 0xFF3F3F3F, false);
                int jbY = y3 + 10;
                canvas.fill(left, jbY, right, jbY + 2, 0xFF9E9E9E);
                int jumpW = (int) Math.round(barW * stats.jumpRatio());
                if (jumpW > 0) {
                    canvas.fill(left, jbY, left + jumpW, jbY + 2, 0xFF479836);
                }
            }
        } else {
            drawStatsPill(canvas, stats, 176, -12, false);
        }
    }

    private static void drawSeparatorDot(Canvas canvas, int x, int y) {
        canvas.fill(x + 1, y + 1, x + 3, y + 3, 0x44000000);
        canvas.fill(x, y, x + 2, y + 2, 0xBBFFFFFF);
    }

    private static void drawSpeedIcon(Canvas canvas, int x, int y, int color) {
        for (int c = 0; c < 2; c++) {
            int ox = x + c * 4;
            canvas.fill(ox, y, ox + 2, y + 1, color);
            canvas.fill(ox + 1, y + 1, ox + 3, y + 2, color);
            canvas.fill(ox + 2, y + 2, ox + 4, y + 3, color);
            canvas.fill(ox + 1, y + 3, ox + 3, y + 4, color);
            canvas.fill(ox, y + 4, ox + 2, y + 5, color);
        }
    }

    private static void drawJumpIcon(Canvas canvas, int x, int y, int color) {
        canvas.fill(x + 3, y, x + 5, y + 1, color);
        canvas.fill(x + 2, y + 1, x + 6, y + 2, color);
        canvas.fill(x + 1, y + 2, x + 7, y + 3, color);
        canvas.fill(x + 3, y + 3, x + 5, y + 7, color);
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
                    } catch (ReflectiveOperationException | RuntimeException ignored) {
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
