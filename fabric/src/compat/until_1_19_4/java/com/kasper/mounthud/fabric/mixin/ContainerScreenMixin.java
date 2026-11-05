package com.kasper.mounthud.fabric.mixin;

import com.kasper.mounthud.client.RidingHud;
import com.kasper.mounthud.compat.Canvas;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Draws the mount's health, speed and jump height in the horse inventory screen (Minecraft 1.16.5 - 1.19.4). */
@Mixin(AbstractContainerScreen.class)
abstract class ContainerScreenMixin {
    @Inject(method = "renderLabels", at = @At("TAIL"))
    private void mounthud$inventoryStats(PoseStack pose, int mouseX, int mouseY, CallbackInfo info) {
        RidingHud.drawInventoryStats(new Canvas(pose), (Screen) (Object) this);
    }
}
