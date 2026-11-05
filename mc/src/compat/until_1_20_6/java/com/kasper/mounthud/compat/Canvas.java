package com.kasper.mounthud.compat;

import com.kasper.mounthud.client.HudSprite;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/** The HUD's drawing surface (Minecraft 1.20.2 - 1.20.6: sprites, before ResourceLocation.withDefaultNamespace). */
public final class Canvas {
    private static final ResourceLocation[] SPRITES = new ResourceLocation[HudSprite.values().length];

    static {
        for (HudSprite sprite : HudSprite.values()) {
            SPRITES[sprite.ordinal()] = new ResourceLocation(sprite.path);
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
