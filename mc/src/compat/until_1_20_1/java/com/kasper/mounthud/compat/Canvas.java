package com.kasper.mounthud.compat;

import com.kasper.mounthud.client.HudSprite;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/** The HUD's drawing surface (Minecraft 1.20.1: the HUD icons are all in icons.png). */
public final class Canvas {
    private static final ResourceLocation ICONS = new ResourceLocation("textures/gui/icons.png");

    private final GuiGraphics graphics;

    public Canvas(GuiGraphics graphics) {
        this.graphics = graphics;
    }

    public int width() {
        return graphics.guiWidth();
    }

    public int height() {
        return graphics.guiHeight();
    }

    /** Draws a 9x9 HUD sprite. */
    public void sprite(HudSprite sprite, int x, int y) {
        RenderSystem.enableBlend();
        graphics.blit(ICONS, x, y, sprite.u, sprite.v, 9, 9);
        RenderSystem.disableBlend();
    }
}
