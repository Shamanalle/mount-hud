package com.kasper.mounthud.compat;

import java.util.function.Function;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.network.chat.Component;

/** Vanilla widgets whose builders changed between versions (Minecraft 1.21.10 and older). */
public final class Widgets {
    private Widgets() {
    }

    /** A button that cycles through values, starting at {@code initial}. */
    public static <T> CycleButton.Builder<T> cycle(Function<T, Component> name, T initial) {
        return CycleButton.builder(name).withInitialValue(initial);
    }
}
