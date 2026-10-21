package com.kasper.mounthud.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Test calls that differ between Minecraft versions. */
final class TestCompat {
    private TestCompat() {
    }

    static void waitForChunks(TestSingleplayerContext world) {
        world.getClientLevel().waitForChunksRender();
    }

    static boolean overlayGone(Minecraft mc) {
        return mc.getOverlay() == null;
    }

    static void clearChat(Minecraft mc) {
        mc.gui.getChat().clearMessages(false);
    }

    /** Sets the GUI scale (0 is automatic) and lays the HUD and the open screen out again. */
    static void guiScale(Minecraft mc, int scale) {
        mc.options.guiScale().set(scale);
        mc.resizeGui();
    }

    /** The open screen, or null. */
    static Screen screen(Minecraft mc) {
        return mc.screen;
    }
}
