package com.kasper.mounthud.neoforge;

import com.kasper.mounthud.client.RidingHud;
import com.kasper.mounthud.compat.Canvas;
import com.kasper.mounthud.hud.HudPlan;
import com.kasper.mounthud.hud.MountHearts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Hud;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * NeoForge draws the HUD as named layers (VanillaGuiLayers), each stacking its rows on Hud's leftHeight and
 * rightHeight (Minecraft 26.2 and newer). The vehicle health layer is wrapped through RegisterGuiLayersEvent so the
 * mount's hearts are drawn above hunger on the right or above armor on the left.
 */
public final class HudHook {
    private HudHook() {
    }

    public static void registerLayers(RegisterGuiLayersEvent event) {
        event.wrapLayer(VanillaGuiLayers.VEHICLE_HEALTH, layer -> (graphics, deltaTracker) -> {
            layer.render(graphics, deltaTracker);
            HudPlan plan = RidingHud.plan();
            if (RidingHud.drawsMountHearts(plan)) {
                drawMountHearts(new Canvas(graphics), plan);
            }
        });
    }

    private static void drawMountHearts(Canvas canvas, HudPlan plan) {
        Minecraft minecraft = Minecraft.getInstance();
        LivingEntity mount = RidingHud.mount();
        if (mount == null || minecraft.gameRenderer.gameRenderState().guiRenderState.isHudHidden) {
            return;
        }
        Hud hud = minecraft.gui.hud;
        int rows = plan.heartRows * MountHearts.ROW_HEIGHT;
        if (plan.hearts == HudPlan.Hearts.LEFT) {
            RidingHud.drawMountHearts(canvas, mount, canvas.width() / 2 - 91, canvas.height() - hud.leftHeight, true);
            hud.leftHeight += rows;
        } else {
            RidingHud.drawMountHearts(canvas, mount, canvas.width() / 2 + 91, canvas.height() - hud.rightHeight, false);
            hud.rightHeight += rows;
        }
    }
}
