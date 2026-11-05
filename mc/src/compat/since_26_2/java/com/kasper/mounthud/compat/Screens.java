package com.kasper.mounthud.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Opening a screen, which moved from Minecraft to its Gui in 26.2 (Minecraft 26.2 and newer). */
public final class Screens {
    private Screens() {
    }

    /** Opens {@code screen}, or closes the open one for {@code null}. */
    public static void open(Minecraft minecraft, Screen screen) {
        minecraft.gui.setScreen(screen);
    }
}
