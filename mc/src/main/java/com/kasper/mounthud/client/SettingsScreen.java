package com.kasper.mounthud.client;

import com.kasper.mounthud.MountHud;
import com.kasper.mounthud.compat.Canvas;
import com.kasper.mounthud.compat.ModScreen;
import com.kasper.mounthud.compat.Screens;
import com.kasper.mounthud.compat.Widgets;
import com.kasper.mounthud.config.HeartsPosition;
import com.kasper.mounthud.config.JumpBarMode;
import com.kasper.mounthud.config.MountStatsMode;
import com.kasper.mounthud.config.Settings;
import com.kasper.mounthud.hud.HudPlan;
import com.kasper.mounthud.hud.HudPlanner;
import com.kasper.mounthud.hud.HudState;
import com.kasper.mounthud.hud.MountHearts;
import com.kasper.mounthud.stats.MountStats;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;

/**
 * The settings screen, opened from Mod Menu (Fabric) or the mod list (Forge, NeoForge), with a live miniature riding
 * HUD preview card that updates immediately as each setting changes.
 */
public final class SettingsScreen extends ModScreen {
    private static final int WIDTH = 240;
    private static final int PREVIEW_HEIGHT = 68;
    private static final Settings DEFAULTS = new Settings();
    private static final MountStats PREVIEW_STATS = new MountStats(23f, 30f, 0.225, 0.7);

    private final Screen parent;
    private final Settings settings;
    private final long openedAt = System.currentTimeMillis();

    private AbstractWidget hungerToggle;
    private AbstractWidget heartsPosition;
    private AbstractWidget jumpBar;
    private AbstractWidget mountStats;
    private AbstractWidget resetButton;

    private int titleY;
    private int previewY;
    private boolean showPreview;

    public SettingsScreen(Screen parent) {
        super(Widgets.text("mounthud.settings.title"));
        this.parent = parent;
        this.settings = MountHud.settings().copy();
    }

    @Override
    protected void init() {
        int x = (width - WIDTH) / 2;
        showPreview = height >= 232;
        int rowStep = height >= 280 ? 24 : 22;
        int gap = height >= 280 ? 8 : 6;
        int titleGap = height >= 280 ? 14 : 13;
        int footerExtra = height >= 280 ? 6 : 4;
        int blockHeight = (showPreview ? titleGap + PREVIEW_HEIGHT + gap : 16) + 5 * rowStep + footerExtra + 20;
        int top = height >= 310
                ? Math.max(12, (height - blockHeight) / 2 - 14)
                : Math.max(4, (height - blockHeight) / 2);

        titleY = top;
        int y = top + (showPreview ? titleGap : 16);
        if (showPreview) {
            previewY = y;
            y += PREVIEW_HEIGHT + gap;
        }

        addRenderableWidget(Widgets.onOff(x, y, WIDTH, 20,
                Widgets.text("mounthud.option.enabled"),
                Widgets.text("mounthud.option.enabled.tooltip"),
                settings.enabled,
                value -> {
                    settings.enabled = value;
                    applyAndSync();
                }));
        y += rowStep;

        hungerToggle = addRenderableWidget(Widgets.onOff(x, y, WIDTH, 20,
                Widgets.text("mounthud.option.hunger_while_riding"),
                Widgets.text("mounthud.option.hunger_while_riding.tooltip"),
                settings.hungerWhileRiding,
                value -> {
                    settings.hungerWhileRiding = value;
                    applyAndSync();
                }));
        y += rowStep;

        heartsPosition = addRenderableWidget(Widgets.cycle(x, y, WIDTH, 20,
                Widgets.text("mounthud.option.hearts_position"),
                Widgets.text("mounthud.option.hearts_position.tooltip"),
                HeartsPosition.values(),
                settings.heartsPosition,
                value -> Widgets.text(value.key()),
                value -> {
                    settings.heartsPosition = value;
                    applyAndSync();
                }));
        y += rowStep;

        jumpBar = addRenderableWidget(Widgets.cycle(x, y, WIDTH, 20,
                Widgets.text("mounthud.option.jump_bar"),
                Widgets.text("mounthud.option.jump_bar.tooltip"),
                JumpBarMode.values(),
                settings.jumpBar,
                value -> Widgets.text(value.key()),
                value -> {
                    settings.jumpBar = value;
                    applyAndSync();
                }));
        y += rowStep;

        mountStats = addRenderableWidget(Widgets.cycle(x, y, WIDTH, 20,
                Widgets.text("mounthud.option.mount_stats"),
                Widgets.text("mounthud.option.mount_stats.tooltip"),
                MountStatsMode.values(),
                settings.mountStats,
                value -> Widgets.text(value.key()),
                value -> {
                    settings.mountStats = value;
                    applyAndSync();
                }));
        y += rowStep + footerExtra;

        int half = (WIDTH - 4) / 2;
        resetButton = addRenderableWidget(Widgets.button(x, y, half, 20, Widgets.text("mounthud.settings.reset"), () -> {
            MountHud.apply(new Settings());
            Screens.open(minecraft, new SettingsScreen(parent));
        }));
        addRenderableWidget(Widgets.button(x + WIDTH - half, y, half, 20, Widgets.text("gui.done"), this::onClose));

        syncWidgets();
    }

    private void applyAndSync() {
        MountHud.apply(settings.copy());
        syncWidgets();
    }

    private void syncWidgets() {
        hungerToggle.active = settings.enabled;
        heartsPosition.active = settings.enabled && settings.hungerWhileRiding;
        jumpBar.active = settings.enabled;
        mountStats.active = settings.enabled;
        resetButton.active = !settings.equals(DEFAULTS);
    }

    @Override
    protected void draw(Canvas canvas, int mouseX, int mouseY) {
        String heading = I18n.get("mounthud.settings.title");
        canvas.text(heading, (width - canvas.textWidth(heading)) / 2, titleY, 0xFFFFFFFF, true);
        if (showPreview) {
            drawPreview(canvas, (width - WIDTH) / 2, previewY);
        }
    }

    private void drawPreview(Canvas canvas, int px, int py) {
        // Framed dark preview card
        canvas.fill(px - 1, py - 1, px + WIDTH + 1, py + PREVIEW_HEIGHT + 1, 0x70000000);
        canvas.fill(px, py, px + WIDTH, py + PREVIEW_HEIGHT, 0xFF424952);
        canvas.fill(px + 1, py + 1, px + WIDTH - 1, py + PREVIEW_HEIGHT - 1, 0xCC12161C);
        canvas.fill(px + 1, py + PREVIEW_HEIGHT - 12, px + WIDTH - 1, py + PREVIEW_HEIGHT - 1, 0x38000000);

        HudState previewState = new HudState();
        previewState.survivalBars = true;
        previewState.experience = true;
        previewState.mount(PREVIEW_STATS.health, PREVIEW_STATS.maxHealth);

        long phase = Math.max(0L, System.currentTimeMillis() - openedAt) % 3600L;
        float charge = 0f;
        boolean cooldown = false;
        if (!settings.enabled || settings.jumpBar == JumpBarMode.ALWAYS_JUMP) {
            charge = 0.78f;
        } else if (settings.jumpBar == JumpBarMode.AUTO) {
            if (phase >= 2100L && phase < 3300L) {
                charge = 0.1f + 0.9f * ((phase - 2100L) / 1200f);
            } else if (phase >= 3300L) {
                cooldown = true;
            }
        }
        previewState.jumping(charge, cooldown);

        HudPlan plan = HudPlanner.plan(settings, previewState);
        int cx = px + WIDTH / 2;
        int left = cx - 91;
        int right = cx + 91;
        int barY = py + PREVIEW_HEIGHT - 9;
        int row0 = barY - 11;
        int row1 = row0 - MountHearts.ROW_HEIGHT;

        // Left side: 10 player hearts + 10 armor icons
        for (int i = 0; i < 10; i++) {
            int hx = left + i * MountHearts.SPACING;
            canvas.sprite(HudSprite.HEART_CONTAINER, hx, row0);
            canvas.sprite(HudSprite.HEART_FULL, hx, row0);
            canvas.sprite(i < 6 ? HudSprite.ARMOR_FULL : HudSprite.ARMOR_EMPTY, hx, row1);
        }

        // Right side: hunger bar (when kept by Mount HUD)
        if (plan.hunger) {
            for (int i = 0; i < 10; i++) {
                int fx = right - i * MountHearts.SPACING - 9;
                canvas.sprite(HudSprite.FOOD_EMPTY, fx, row0);
                if (i < 7) {
                    canvas.sprite(HudSprite.FOOD_FULL, fx, row0);
                } else if (i == 7) {
                    canvas.sprite(HudSprite.FOOD_HALF, fx, row0);
                }
            }
        }

        // Mount hearts + optional HUD stats pill
        if (plan.hearts == HudPlan.Hearts.LEFT) {
            int heartsY = row1 - MountHearts.ROW_HEIGHT;
            RidingHud.drawMountHearts(canvas, PREVIEW_STATS.health, PREVIEW_STATS.maxHealth, left, heartsY, true);
            if (plan.stats) {
                RidingHud.drawStatsPill(canvas, PREVIEW_STATS, left, heartsY - plan.heartRows * MountHearts.ROW_HEIGHT, true);
            }
        } else if (plan.hearts == HudPlan.Hearts.ABOVE_HUNGER) {
            RidingHud.drawMountHearts(canvas, PREVIEW_STATS.health, PREVIEW_STATS.maxHealth, right, row1, false);
            if (plan.stats) {
                RidingHud.drawStatsPill(canvas, PREVIEW_STATS, right, row1 - plan.heartRows * MountHearts.ROW_HEIGHT, false);
            }
        } else if (plan.hearts == HudPlan.Hearts.VANILLA) {
            RidingHud.drawMountHearts(canvas, PREVIEW_STATS.health, PREVIEW_STATS.maxHealth, right, row0, false);
            if (plan.stats) {
                RidingHud.drawStatsPill(canvas, PREVIEW_STATS, right, row0 - plan.heartRows * MountHearts.ROW_HEIGHT, false);
            }
        }

        // Air bubbles on the right, showing how they automatically rise above right-hand mount hearts and stats
        int airY = row1 - plan.airRowsUp * MountHearts.ROW_HEIGHT;
        if (airY >= py + 3) {
            for (int i = 0; i < 5; i++) {
                canvas.sprite(HudSprite.AIR, right - i * MountHearts.SPACING - 9, airY);
            }
        }

        // Experience / jump bar at the bottom of the preview card
        canvas.fill(left, barY, right, barY + 5, 0xFF222624);
        canvas.fill(left + 1, barY + 1, right - 1, barY + 4, 0xFF0D100F);
        if (plan.bar == HudPlan.Bar.EXPERIENCE) {
            int fillW = 118;
            canvas.fill(left + 1, barY + 1, left + 1 + fillW, barY + 4, 0xFF63D91B);
            canvas.fill(left + 1, barY + 1, left + 1 + fillW, barY + 2, 0xFFA8FF59);
            for (int sx = left + 18; sx < right - 2; sx += 18) {
                canvas.fill(sx, barY + 1, sx + 1, barY + 4, 0x48000000);
            }
            String level = "7";
            canvas.text(level, cx - canvas.textWidth(level) / 2, barY - 6, 0xFF80FF20, true);
        } else if (plan.bar == HudPlan.Bar.JUMP) {
            float shown = cooldown ? 0.22f : (charge > 0 ? charge : 0.78f);
            int fillW = Math.max(2, Math.min(180, Math.round(180f * shown)));
            int sweetSpot = 154;
            int blueW = Math.min(fillW, sweetSpot);
            canvas.fill(left + 1, barY + 1, left + 1 + blueW, barY + 4, cooldown ? 0xFF3E6878 : 0xFF28A8DC);
            canvas.fill(left + 1, barY + 1, left + 1 + blueW, barY + 2, cooldown ? 0xFF638E9E : 0xFF7AE0FF);
            if (fillW > sweetSpot) {
                canvas.fill(left + 1 + sweetSpot, barY + 1, left + 1 + fillW, barY + 4, 0xFFE88624);
                canvas.fill(left + 1 + sweetSpot, barY + 1, left + 1 + fillW, barY + 2, 0xFFFFBE5C);
            } else {
                canvas.fill(left + 1 + sweetSpot, barY + 1, left + 2 + sweetSpot, barY + 4, 0x66FFAA44);
            }
        }
    }

    @Override
    public void onClose() {
        MountHud.apply(settings);
        Screens.open(minecraft, parent);
    }
}
