package com.kasper.mounthud.compat;

import com.kasper.mounthud.client.HudSprite;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/** The HUD's drawing surface (Minecraft 1.21.11: ResourceLocation is now Identifier). */
public final class Canvas {
    private static final Identifier[] SPRITES = new Identifier[HudSprite.values().length];

    static {
        for (HudSprite sprite : HudSprite.values()) {
            SPRITES[sprite.ordinal()] = Identifier.withDefaultNamespace(sprite.path);
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
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITES[sprite.ordinal()], x, y, 9, 9);
    }

    public int textWidth(String text) {
        return Minecraft.getInstance().font.width(text);
    }

    public void text(String text, int x, int y, int color, boolean shadow) {
        graphics.drawString(Minecraft.getInstance().font, text, x, y, color, shadow);
    }
}
