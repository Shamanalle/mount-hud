package com.kasper.mounthud.compat;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Screen base class (Minecraft 1.20.2 - 1.21.11: Screen.render draws the background). */
public abstract class ModScreen extends Screen {
    protected ModScreen(Component title) {
        super(title);
    }

    protected void draw(Canvas canvas, int mouseX, int mouseY) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        draw(new Canvas(graphics), mouseX, mouseY);
    }
}
