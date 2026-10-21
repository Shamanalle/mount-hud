package com.kasper.mounthud.neoforge.mixin;

import com.kasper.mounthud.client.RidingHud;
import com.kasper.mounthud.compat.Canvas;
import com.kasper.mounthud.hud.HudPlan;
import com.kasper.mounthud.hud.MountHearts;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PlayerRideableJumping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vanilla's riding HUD, as the plan wants it (NeoForge 1.21.1 and older, whose HUD layers stack their rows on
 * leftHeight and rightHeight). Vanilla is told the mount has no hearts, so it draws hunger, and the mod draws the
 * hearts in the vehicle health layer: on the right above hunger, where the air layer that follows stacks above them,
 * or on the left above armor. On a mount that jumps vanilla is told there is no such mount, so it shows experience.
 */
@Mixin(Gui.class)
abstract class HudMixin {
    @Shadow(remap = false)
    public int leftHeight;
    @Shadow(remap = false)
    public int rightHeight;

    @ModifyReturnValue(method = "getVehicleMaxHearts", at = @At("RETURN"))
    private int mounthud$vehicleHearts(int hearts) {
        return hearts != 0 && RidingHud.drawsMountHearts(RidingHud.plan()) ? 0 : hearts;
    }

    @Inject(method = "renderVehicleHealth", at = @At("HEAD"))
    private void mounthud$mountHearts(GuiGraphics graphics, CallbackInfo info) {
        HudPlan plan = RidingHud.plan();
        LivingEntity mount = RidingHud.mount();
        if (mount == null || !RidingHud.drawsMountHearts(plan)) {
            return;
        }
        Canvas canvas = new Canvas(graphics);
        int rows = plan.heartRows * MountHearts.ROW_HEIGHT;
        if (plan.hearts == HudPlan.Hearts.LEFT) {
            RidingHud.drawMountHearts(canvas, mount, graphics.guiWidth() / 2 - 91, graphics.guiHeight() - leftHeight, true);
            leftHeight += rows;
        } else {
            RidingHud.drawMountHearts(canvas, mount, graphics.guiWidth() / 2 + 91, graphics.guiHeight() - rightHeight, false);
            rightHeight += rows;
        }
    }

    @ModifyExpressionValue(method = {"maybeRenderJumpMeter", "maybeRenderExperienceBar", "isExperienceBarVisible"},
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;jumpableVehicle()Lnet/minecraft/world/entity/PlayerRideableJumping;"))
    private PlayerRideableJumping mounthud$jumpBar(PlayerRideableJumping vehicle) {
        return vehicle != null && RidingHud.hidesJumpBar(RidingHud.plan()) ? null : vehicle;
    }
}
