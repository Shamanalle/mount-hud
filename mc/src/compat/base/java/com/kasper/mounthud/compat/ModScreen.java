package com.kasper.mounthud.compat;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Screen base class (Minecraft 1.20.2 and newer: Screen.render draws the background). */
public abstract class ModScreen extends Screen {
    protected ModScreen(Component title) {
        super(title);
    }
}
