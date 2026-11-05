package com.kasper.mounthud.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/** Vanilla widgets whose builders changed between versions (Minecraft 1.19 - 1.19.2). */
public final class Widgets {
    private Widgets() {
    }

    public static Component text(String key) {
        return Component.translatable(key);
    }

    public static AbstractWidget title(Font font, Component title, int x, int y, int width, int height) {
        AbstractWidget widget = new AbstractWidget(x, y, width, height, title) {
            @Override
            public void renderButton(PoseStack pose, int mouseX, int mouseY, float partialTick) {
                drawCenteredString(pose, font, getMessage(), this.x + this.width / 2, this.y, 0xFFFFFF);
            }

            @Override
            public void updateNarration(NarrationElementOutput output) {
            }
        };
        widget.active = false;
        return widget;
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
