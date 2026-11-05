package com.kasper.mounthud.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Screen base class (Minecraft 1.16.5: addButton instead of addRenderableWidget). */
public abstract class ModScreen extends Screen {
    private int titleY;

    protected ModScreen(Component title) {
        super(title);
    }

    protected final void addTitle(int x, int y, int width, int height) {
        this.titleY = y;
    }

    protected final <T extends AbstractWidget> T addRenderableWidget(T widget) {
        return addButton(widget);
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        renderBackground(pose);
        drawCenteredString(pose, font, title, width / 2, titleY, 0xFFFFFF);
        super.render(pose, mouseX, mouseY, partialTick);
    }
}
