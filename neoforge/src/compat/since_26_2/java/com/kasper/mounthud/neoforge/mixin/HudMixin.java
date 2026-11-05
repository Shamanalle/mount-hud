package com.kasper.mounthud.neoforge.mixin;

import com.kasper.mounthud.client.RidingHud;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.gui.Hud;
import net.minecraft.world.entity.PlayerRideableJumping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Vanilla's riding HUD, as the plan wants it (NeoForge 26.2 and newer, where Gui's HUD moved to Hud). Vanilla is told
 * the mount has no hearts, so the food layer draws hunger and HudHook draws the mount's hearts in the vehicle health
 * layer. On a mount that jumps vanilla is told there is no such mount, so the contextual bar shows experience.
 */
@Mixin(Hud.class)
abstract class HudMixin {
    @ModifyReturnValue(method = "getVehicleMaxHearts", at = @At("RETURN"))
    private int mounthud$vehicleHearts(int hearts) {
        return hearts != 0 && RidingHud.drawsMountHearts(RidingHud.plan()) ? 0 : hearts;
    }

    @ModifyExpressionValue(method = {"nextContextualInfoState"},
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;jumpableVehicle()Lnet/minecraft/world/entity/PlayerRideableJumping;"))
    private PlayerRideableJumping mounthud$jumpBar(PlayerRideableJumping vehicle) {
        return vehicle != null && RidingHud.hidesJumpBar(RidingHud.plan()) ? null : vehicle;
    }
}
