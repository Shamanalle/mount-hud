package com.kasper.mounthud.compat;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Screen base class (Minecraft 1.20 - 1.20.1: renderBackground must be called before super.render). */
public abstract class ModScreen extends Screen {
    protected ModScreen(Component title) {
        super(title);
    }

    protected void draw(Canvas canvas, int mouseX, int mouseY) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        draw(new Canvas(graphics), mouseX, mouseY);
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}
