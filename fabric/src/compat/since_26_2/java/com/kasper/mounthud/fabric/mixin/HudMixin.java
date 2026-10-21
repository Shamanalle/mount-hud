package com.kasper.mounthud.fabric.mixin;

import com.kasper.mounthud.client.RidingHud;
import com.kasper.mounthud.compat.Canvas;
import com.kasper.mounthud.hud.HudPlan;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.world.entity.PlayerRideableJumping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vanilla's riding HUD, as the plan wants it. Vanilla is told the mount has no hearts, so it draws hunger where they
 * were, and the mod draws them itself; the air bubbles move up by the rows the hearts take above hunger. On a mount
 * that jumps vanilla shows the jump bar instead of experience: told there is no such mount, it shows experience.
 */
@Mixin(Hud.class)
abstract class HudMixin {
    @ModifyReturnValue(method = "getVehicleMaxHearts", at = @At("RETURN"))
    private int mounthud$vehicleHearts(int hearts) {
        return hearts != 0 && RidingHud.drawsMountHearts(RidingHud.plan()) ? 0 : hearts;
    }

    @ModifyReturnValue(method = "getVisibleVehicleHeartRows", at = @At("RETURN"))
    private int mounthud$airRows(int rows) {
        HudPlan plan = RidingHud.plan();
        return RidingHud.drawsMountHearts(plan) ? plan.airRowsUp : rows;
    }

    @Inject(method = "extractVehicleHealth", at = @At("HEAD"))
    private void mounthud$mountHearts(GuiGraphicsExtractor graphics, CallbackInfo info) {
        HudPlan plan = RidingHud.plan();
        if (RidingHud.drawsMountHearts(plan)) {
            RidingHud.drawMountHearts(new Canvas(graphics), plan);
        }
    }

    @ModifyExpressionValue(method = {"nextContextualInfoState"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;jumpableVehicle()Lnet/minecraft/world/entity/PlayerRideableJumping;"))
    private PlayerRideableJumping mounthud$jumpBar(PlayerRideableJumping vehicle) {
        return vehicle != null && RidingHud.hidesJumpBar(RidingHud.plan()) ? null : vehicle;
    }
}
