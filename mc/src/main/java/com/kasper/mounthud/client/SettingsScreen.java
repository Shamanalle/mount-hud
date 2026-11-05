package com.kasper.mounthud.client;

import com.kasper.mounthud.MountHud;
import com.kasper.mounthud.compat.ModScreen;
import com.kasper.mounthud.compat.Screens;
import com.kasper.mounthud.compat.Widgets;
import com.kasper.mounthud.config.HeartsPosition;
import com.kasper.mounthud.config.JumpBarMode;
import com.kasper.mounthud.config.MountStatsMode;
import com.kasper.mounthud.config.Settings;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;

/**
 * The settings screen, opened from Mod Menu (Fabric) or the mod list (Forge, NeoForge). It is made of vanilla
 * widgets only and draws nothing itself, so it is the same code on every Minecraft version.
 */
public final class SettingsScreen extends ModScreen {
    private static final int WIDTH = 220;
    private static final int ROW = 24;

    private final Screen parent;
    private final Settings settings;
    private AbstractWidget heartsPosition;

    public SettingsScreen(Screen parent) {
        super(Widgets.text("mounthud.settings.title"));
        this.parent = parent;
        this.settings = MountHud.settings().copy();
    }

    @Override
    protected void init() {
        int x = (width - WIDTH) / 2;
        int y = Math.max(24, height / 2 - 4 * ROW - 12);
        addTitle(x, y - 20, WIDTH, 12);

        addRenderableWidget(Widgets.onOff(x, y, WIDTH, 20,
                Widgets.text("mounthud.option.enabled"),
                Widgets.text("mounthud.option.enabled.tooltip"),
                settings.enabled,
                value -> settings.enabled = value));
        y += ROW;
        addRenderableWidget(Widgets.onOff(x, y, WIDTH, 20,
                Widgets.text("mounthud.option.hunger_while_riding"),
                Widgets.text("mounthud.option.hunger_while_riding.tooltip"),
                settings.hungerWhileRiding,
                value -> {
                    settings.hungerWhileRiding = value;
                    heartsPosition.active = value;
                }));
        y += ROW;
        heartsPosition = addRenderableWidget(Widgets.cycle(x, y, WIDTH, 20,
                Widgets.text("mounthud.option.hearts_position"),
                Widgets.text("mounthud.option.hearts_position.tooltip"),
                HeartsPosition.values(),
                settings.heartsPosition,
                value -> Widgets.text(value.key()),
                value -> settings.heartsPosition = value));
        heartsPosition.active = settings.hungerWhileRiding;
        y += ROW;
        addRenderableWidget(Widgets.cycle(x, y, WIDTH, 20,
                Widgets.text("mounthud.option.jump_bar"),
                Widgets.text("mounthud.option.jump_bar.tooltip"),
                JumpBarMode.values(),
                settings.jumpBar,
                value -> Widgets.text(value.key()),
                value -> settings.jumpBar = value));
        y += ROW;
        addRenderableWidget(Widgets.cycle(x, y, WIDTH, 20,
                Widgets.text("mounthud.option.mount_stats"),
                Widgets.text("mounthud.option.mount_stats.tooltip"),
                MountStatsMode.values(),
                settings.mountStats,
                value -> Widgets.text(value.key()),
                value -> settings.mountStats = value));
        y += ROW + 12;

        int half = (WIDTH - 4) / 2;
        addRenderableWidget(Widgets.button(x, y, half, 20, Widgets.text("mounthud.settings.reset"), () -> {
            MountHud.apply(new Settings());
            Screens.open(minecraft, new SettingsScreen(parent));
        }));
        addRenderableWidget(Widgets.button(x + WIDTH - half, y, half, 20, Widgets.text("gui.done"), this::onClose));
    }

    @Override
    public void onClose() {
        MountHud.apply(settings);
        Screens.open(minecraft, parent);
    }
}
