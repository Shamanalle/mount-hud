package com.kasper.mounthud.compat;

import com.kasper.mounthud.client.HudSprite;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/** The HUD's drawing surface (Minecraft 1.21.2 - 1.21.5: sprites drawn with a render type). */
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

    /** Draws a 9x9 HUD sprite. */
    public void sprite(HudSprite sprite, int x, int y) {
        graphics.blitSprite(RenderType::guiTextured, SPRITES[sprite.ordinal()], x, y, 9, 9);
    }
}
