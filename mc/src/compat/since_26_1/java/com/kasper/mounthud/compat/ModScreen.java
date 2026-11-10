package com.kasper.mounthud.compat;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Screen base class (Minecraft 26.1 and newer: extractRenderState replaces render). */
public abstract class ModScreen extends Screen {
    protected ModScreen(Component title) {
        super(title);
    }

    protected void draw(Canvas canvas, int mouseX, int mouseY) {
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        draw(new Canvas(graphics), mouseX, mouseY);
    }
}
