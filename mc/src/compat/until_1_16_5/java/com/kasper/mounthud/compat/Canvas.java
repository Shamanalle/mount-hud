package com.kasper.mounthud.compat;

import com.kasper.mounthud.client.HudSprite;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;

/** The HUD's drawing surface (Minecraft 1.16.5: PoseStack + TextureManager.bind). */
public final class Canvas {
    private static final ResourceLocation ICONS = new ResourceLocation("textures/gui/icons.png");

    private final PoseStack pose;

    public Canvas(PoseStack pose) {
        this.pose = pose;
    }

    public int width() {
        return Minecraft.getInstance().getWindow().getGuiScaledWidth();
    }

    public int height() {
        return Minecraft.getInstance().getWindow().getGuiScaledHeight();
    }

    public void fill(int x1, int y1, int x2, int y2, int color) {
        GuiComponent.fill(pose, x1, y1, x2, y2, color);
    }

    /** Draws a 9x9 HUD sprite. */
    public void sprite(HudSprite sprite, int x, int y) {
        RenderSystem.enableBlend();
        Minecraft.getInstance().getTextureManager().bind(ICONS);
        GuiComponent.blit(pose, x, y, sprite.u, sprite.v, 9, 9, 256, 256);
        RenderSystem.disableBlend();
    }

    public int textWidth(String text) {
        return Minecraft.getInstance().font.width(text);
    }

    public void text(String text, int x, int y, int color, boolean shadow) {
        Font font = Minecraft.getInstance().font;
        if (shadow) {
            font.drawShadow(pose, text, x, y, color);
        } else {
            font.draw(pose, text, x, y, color);
        }
    }
}
