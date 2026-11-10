package com.kasper.mounthud.compat;

import com.kasper.mounthud.client.HudSprite;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/** The HUD's drawing surface (Minecraft 1.20.5 - 1.21.1: sprites, blending switched on by hand). */
public final class Canvas {
    private static final ResourceLocation[] SPRITES = new ResourceLocation[HudSprite.values().length];

    static {
        for (HudSprite sprite : HudSprite.values()) {
            SPRITES[sprite.ordinal()] = ResourceLocation.withDefaultNamespace(sprite.path);
        }
    }

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

    public void fill(int x1, int y1, int x2, int y2, int color) {
        graphics.fill(x1, y1, x2, y2, color);
    }

    /** Draws a 9x9 HUD sprite. */
    public void sprite(HudSprite sprite, int x, int y) {
        RenderSystem.enableBlend();
        graphics.blitSprite(SPRITES[sprite.ordinal()], x, y, 9, 9);
        RenderSystem.disableBlend();
    }

    public int textWidth(String text) {
        return Minecraft.getInstance().font.width(text);
    }

    public void text(String text, int x, int y, int color, boolean shadow) {
        graphics.drawString(Minecraft.getInstance().font, text, x, y, color, shadow);
    }
}
