package com.kasper.mounthud.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Screen base class (Minecraft 1.17.1 - 1.19.4: PoseStack + renderBackground). */
public abstract class ModScreen extends Screen {
    protected ModScreen(Component title) {
        super(title);
    }

    protected void draw(Canvas canvas, int mouseX, int mouseY) {
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        renderBackground(pose);
        draw(new Canvas(pose), mouseX, mouseY);
        super.render(pose, mouseX, mouseY, partialTick);
    }
}
