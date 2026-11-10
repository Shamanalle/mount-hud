package com.kasper.mounthud.neoforge;

import com.kasper.mounthud.client.RidingHud;
import com.kasper.mounthud.compat.Canvas;
import com.kasper.mounthud.hud.HudPlan;
import com.kasper.mounthud.hud.MountHearts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Hud;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * NeoForge draws the HUD as named layers (VanillaGuiLayers), each stacking its rows on Hud's leftHeight and
 * rightHeight (Minecraft 26.2 and newer). Listening to RenderGuiLayerEvent.Post of VEHICLE_HEALTH draws the mount's
 * hearts and optional stats inside the vanilla vehicle health layer's matrix (so HUD-offset mods such as Raised move
 * them automatically) right before AIR_LEVEL stacks above them.
 */
public final class HudHook {
    private HudHook() {
    }

    public static void afterVehicleHealth(RenderGuiLayerEvent.Post event) {
        if (!VanillaGuiLayers.VEHICLE_HEALTH.equals(event.getName())) {
            return;
        }
        HudPlan plan = RidingHud.plan();
        if (RidingHud.drawsMountHearts(plan) || plan.stats) {
            drawMountHud(new Canvas(event.getGuiGraphics()), plan);
        }
    }

    private static void drawMountHud(Canvas canvas, HudPlan plan) {
        Minecraft minecraft = Minecraft.getInstance();
        LivingEntity mount = RidingHud.mount();
        if (mount == null || minecraft.gameRenderer.gameRenderState().guiRenderState.isHudHidden) {
            return;
        }
        Hud hud = minecraft.gui.hud;
        int rows = plan.heartRows * MountHearts.ROW_HEIGHT;
        boolean left = plan.hearts == HudPlan.Hearts.LEFT;
        if (RidingHud.drawsMountHearts(plan)) {
            if (left) {
                RidingHud.drawMountHearts(canvas, mount, canvas.width() / 2 - 91, canvas.height() - hud.leftHeight, true);
                hud.leftHeight += rows;
            } else {
                RidingHud.drawMountHearts(canvas, mount, canvas.width() / 2 + 91, canvas.height() - hud.rightHeight, false);
                hud.rightHeight += rows;
            }
        }
        if (plan.stats) {
            if (left) {
                RidingHud.drawHudStats(canvas, mount, canvas.width() / 2 - 91, canvas.height() - hud.leftHeight, true);
                hud.leftHeight += MountHearts.ROW_HEIGHT;
            } else {
                RidingHud.drawHudStats(canvas, mount, canvas.width() / 2 + 91, canvas.height() - hud.rightHeight, false);
                hud.rightHeight += MountHearts.ROW_HEIGHT;
            }
        }
    }
}
