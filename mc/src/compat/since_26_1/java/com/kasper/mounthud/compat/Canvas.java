package com.kasper.mounthud.compat;

import com.kasper.mounthud.client.HudSprite;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/** The HUD's drawing surface (Minecraft 26.1 and newer: GuiGraphics is now GuiGraphicsExtractor). */
public final class Canvas {
    private static final Identifier[] SPRITES = new Identifier[HudSprite.values().length];

    static {
        for (HudSprite sprite : HudSprite.values()) {
            SPRITES[sprite.ordinal()] = Identifier.withDefaultNamespace(sprite.path);
        }
    }

    private final GuiGraphicsExtractor graphics;

    public Canvas(GuiGraphicsExtractor graphics) {
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
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITES[sprite.ordinal()], x, y, 9, 9);
    }
}
