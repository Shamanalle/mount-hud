package com.kasper.mounthud.client;

import com.kasper.mounthud.MountHud;
import com.kasper.mounthud.compat.Screens;
import com.kasper.mounthud.compat.Widgets;
import com.kasper.mounthud.config.HeartsPosition;
import com.kasper.mounthud.config.JumpBarMode;
import com.kasper.mounthud.config.Settings;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * The settings screen, opened from Mod Menu (Fabric) or the mod list (Forge, NeoForge). It is made of vanilla
 * widgets only and draws nothing itself, so it is the same code on every Minecraft version.
 */
public final class SettingsScreen extends Screen {
    private static final int WIDTH = 220;
    private static final int ROW = 24;

    private final Screen parent;
    private final Settings settings;
    private CycleButton<HeartsPosition> heartsPosition;

    public SettingsScreen(Screen parent) {
        super(Component.translatable("mounthud.settings.title"));
        this.parent = parent;
        this.settings = MountHud.settings().copy();
    }

    @Override
    protected void init() {
        int x = (width - WIDTH) / 2;
        int y = Math.max(24, height / 2 - 4 * ROW);
        addRenderableWidget(new StringWidget(x, y - 20, WIDTH, 12, title, font));

        addRenderableWidget(CycleButton.onOffBuilder(settings.enabled)
                .withTooltip(value -> tooltip("mounthud.option.enabled.tooltip"))
                .create(x, y, WIDTH, 20, Component.translatable("mounthud.option.enabled"),
                        (button, value) -> settings.enabled = value));
        y += ROW;
        addRenderableWidget(CycleButton.onOffBuilder(settings.hungerWhileRiding)
                .withTooltip(value -> tooltip("mounthud.option.hunger_while_riding.tooltip"))
                .create(x, y, WIDTH, 20, Component.translatable("mounthud.option.hunger_while_riding"),
                        (button, value) -> {
                            settings.hungerWhileRiding = value;
                            heartsPosition.active = value;
                        }));
        y += ROW;
        heartsPosition = addRenderableWidget(Widgets.<HeartsPosition>cycle(
                        value -> Component.translatable(value.key()), settings.heartsPosition)
                .withValues(HeartsPosition.values())
                .withTooltip(value -> tooltip("mounthud.option.hearts_position.tooltip"))
                .create(x, y, WIDTH, 20, Component.translatable("mounthud.option.hearts_position"),
                        (button, value) -> settings.heartsPosition = value));
        heartsPosition.active = settings.hungerWhileRiding;
        y += ROW;
        addRenderableWidget(Widgets.<JumpBarMode>cycle(
                        value -> Component.translatable(value.key()), settings.jumpBar)
                .withValues(JumpBarMode.values())
                .withTooltip(value -> tooltip("mounthud.option.jump_bar.tooltip"))
                .create(x, y, WIDTH, 20, Component.translatable("mounthud.option.jump_bar"),
                        (button, value) -> settings.jumpBar = value));
        y += ROW + 12;

        int half = (WIDTH - 4) / 2;
        addRenderableWidget(Button.builder(Component.translatable("mounthud.settings.reset"), button -> {
            MountHud.apply(new Settings());
            Screens.open(minecraft, new SettingsScreen(parent));
        }).bounds(x, y, half, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(x + WIDTH - half, y, half, 20).build());
    }

    private static Tooltip tooltip(String key) {
        return Tooltip.create(Component.translatable(key));
    }

    @Override
    public void onClose() {
        MountHud.apply(settings);
        Screens.open(minecraft, parent);
    }
}
