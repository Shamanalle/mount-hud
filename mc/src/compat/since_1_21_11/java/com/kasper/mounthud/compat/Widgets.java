package com.kasper.mounthud.compat;

import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

/** Vanilla widgets whose builders changed between versions (Minecraft 1.21.11 and newer). */
public final class Widgets {
    private Widgets() {
    }

    public static Component text(String key) {
        return Component.translatable(key);
    }

    public static AbstractWidget onOff(int x, int y, int width, int height, Component label, Component tooltip,
            boolean initial, Consumer<Boolean> onChange) {
        return CycleButton.onOffBuilder(initial)
                .withTooltip(value -> Tooltip.create(tooltip))
                .create(x, y, width, height, label, (button, value) -> onChange.accept(value));
    }

    public static <T> AbstractWidget cycle(int x, int y, int width, int height, Component label, Component tooltip,
            T[] values, T initial, Function<T, Component> name, Consumer<T> onChange) {
        return CycleButton.builder(name, initial)
                .withValues(values)
                .withTooltip(value -> Tooltip.create(tooltip))
                .create(x, y, width, height, label, (button, value) -> onChange.accept(value));
    }

    public static AbstractWidget button(int x, int y, int width, int height, Component label, Runnable onPress) {
        return Button.builder(label, button -> onPress.run()).bounds(x, y, width, height).build();
    }
}
