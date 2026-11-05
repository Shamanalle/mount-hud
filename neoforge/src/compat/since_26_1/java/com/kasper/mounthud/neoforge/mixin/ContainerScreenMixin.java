package com.kasper.mounthud.neoforge.mixin;

import com.kasper.mounthud.client.RidingHud;
import com.kasper.mounthud.compat.Canvas;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Draws the mount's health, speed and jump height in the horse inventory screen (Minecraft 26.1 and newer). */
@Mixin(AbstractContainerScreen.class)
abstract class ContainerScreenMixin {
    @Inject(method = "extractLabels", at = @At("TAIL"))
    private void mounthud$inventoryStats(GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo info) {
        RidingHud.drawInventoryStats(new Canvas(graphics), (Screen) (Object) this);
    }
}
