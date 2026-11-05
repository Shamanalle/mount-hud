package com.kasper.mounthud.compat;

import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

/** Vanilla widgets whose builders changed between versions (Minecraft 1.16.5: Button before CycleButton). */
public final class Widgets {
    private Widgets() {
    }

    public static Component text(String key) {
        return new TranslatableComponent(key);
    }

    public static AbstractWidget onOff(int x, int y, int width, int height, Component label, Component tooltip,
            boolean initial, Consumer<Boolean> onChange) {
        boolean[] state = {initial};
        return new Button(x, y, width, height, CommonComponents.optionStatus(label, initial), button -> {
            state[0] = !state[0];
            button.setMessage(CommonComponents.optionStatus(label, state[0]));
            onChange.accept(state[0]);
        });
    }

    public static <T> AbstractWidget cycle(int x, int y, int width, int height, Component label, Component tooltip,
            T[] values, T initial, Function<T, Component> name, Consumer<T> onChange) {
        int[] index = {0};
        for (int i = 0; i < values.length; i++) {
            if (values[i] == initial) {
                index[0] = i;
                break;
            }
        }
        return new Button(x, y, width, height, cycleMessage(label, name.apply(values[index[0]])), button -> {
            index[0] = (index[0] + 1) % values.length;
            T next = values[index[0]];
            button.setMessage(cycleMessage(label, name.apply(next)));
            onChange.accept(next);
        });
    }

    public static AbstractWidget button(int x, int y, int width, int height, Component label, Runnable onPress) {
        return new Button(x, y, width, height, label, button -> onPress.run());
    }

    private static Component cycleMessage(Component label, Component value) {
        return new TranslatableComponent("options.generic_value", label, value);
    }
}
