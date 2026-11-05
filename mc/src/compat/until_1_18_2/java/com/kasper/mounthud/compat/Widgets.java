package com.kasper.mounthud.compat;

import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

/** Vanilla widgets whose builders changed between versions (Minecraft 1.17.1 - 1.18.2). */
public final class Widgets {
    private Widgets() {
    }

    public static Component text(String key) {
        return new TranslatableComponent(key);
    }

    public static AbstractWidget onOff(int x, int y, int width, int height, Component label, Component tooltip,
            boolean initial, Consumer<Boolean> onChange) {
        return CycleButton.onOffBuilder(initial)
                .create(x, y, width, height, label, (button, value) -> onChange.accept(value));
    }

    public static <T> AbstractWidget cycle(int x, int y, int width, int height, Component label, Component tooltip,
            T[] values, T initial, Function<T, Component> name, Consumer<T> onChange) {
        return CycleButton.builder(name)
                .withValues(values)
                .withInitialValue(initial)
                .create(x, y, width, height, label, (button, value) -> onChange.accept(value));
    }

    public static AbstractWidget button(int x, int y, int width, int height, Component label, Runnable onPress) {
        return new Button(x, y, width, height, label, button -> onPress.run());
    }
}
