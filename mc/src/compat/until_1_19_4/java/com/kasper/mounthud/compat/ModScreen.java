package com.kasper.mounthud.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Screen base class (Minecraft 1.17.1 - 1.19.4: PoseStack + renderBackground). */
public abstract class ModScreen extends Screen {
    protected ModScreen(Component title) {
        super(title);
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        renderBackground(pose);
        super.render(pose, mouseX, mouseY, partialTick);
    }
}
