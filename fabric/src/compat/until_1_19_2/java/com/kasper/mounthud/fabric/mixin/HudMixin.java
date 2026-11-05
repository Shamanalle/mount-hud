package com.kasper.mounthud.fabric.mixin;

import com.kasper.mounthud.client.RidingHud;
import com.kasper.mounthud.compat.Canvas;
import com.kasper.mounthud.hud.HudPlan;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vanilla's riding HUD, as the plan wants it (Minecraft 1.16.5 - 1.19.2: PoseStack + isRidingJumpable()).
 */
@Mixin(Gui.class)
abstract class HudMixin {
    @ModifyReturnValue(method = "getVehicleMaxHearts", at = @At("RETURN"))
    private int mounthud$vehicleHearts(int hearts) {
        return hearts != 0 && RidingHud.drawsMountHearts(RidingHud.plan()) ? 0 : hearts;
    }

    @ModifyReturnValue(method = "getVisibleVehicleHeartRows", at = @At("RETURN"))
    private int mounthud$airRows(int rows) {
        HudPlan plan = RidingHud.plan();
        return RidingHud.drawsMountHearts(plan) || plan.stats ? plan.airRowsUp : rows;
    }

    @Inject(method = "renderVehicleHealth", at = @At("HEAD"))
    private void mounthud$mountHearts(PoseStack pose, CallbackInfo info) {
        HudPlan plan = RidingHud.plan();
        if (RidingHud.drawsMountHearts(plan) || plan.stats) {
            RidingHud.drawMountHearts(new Canvas(pose), plan);
        }
    }

    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;isRidingJumpable()Z"))
    private boolean mounthud$jumpBar(boolean jumping) {
        return jumping && !RidingHud.hidesJumpBar(RidingHud.plan());
    }
}
