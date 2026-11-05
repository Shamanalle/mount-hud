package com.kasper.mounthud.neoforge;

import com.kasper.mounthud.MountHud;
import com.kasper.mounthud.client.RidingHud;
import com.kasper.mounthud.compat.Canvas;
import com.kasper.mounthud.hud.HudPlan;
import com.kasper.mounthud.hud.MountHearts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * NeoForge draws the HUD as named layers (VanillaGuiLayers), each stacking its rows on Gui's leftHeight and
 * rightHeight. Our layer is registered directly above VEHICLE_HEALTH through RegisterGuiLayersEvent so the mount's
 * hearts are drawn above hunger on the right (where the air layer that follows stacks above them) or above armor on
 * the left.
 */
public final class HudHook {
    private static final ResourceLocation LAYER_ID =
            ResourceLocation.fromNamespaceAndPath(MountHud.MOD_ID, "vehicle_health");

    private HudHook() {
    }

    public static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.VEHICLE_HEALTH, LAYER_ID, (graphics, deltaTracker) -> {
            HudPlan plan = RidingHud.plan();
            if (RidingHud.drawsMountHearts(plan)) {
                drawMountHearts(new Canvas(graphics), plan);
            }
        });
    }

    private static void drawMountHearts(Canvas canvas, HudPlan plan) {
        Minecraft minecraft = Minecraft.getInstance();
        LivingEntity mount = RidingHud.mount();
        if (mount == null || minecraft.options.hideGui) {
            return;
        }
        Gui gui = minecraft.gui;
        int rows = plan.heartRows * MountHearts.ROW_HEIGHT;
        if (plan.hearts == HudPlan.Hearts.LEFT) {
            RidingHud.drawMountHearts(canvas, mount, canvas.width() / 2 - 91, canvas.height() - gui.leftHeight, true);
            gui.leftHeight += rows;
        } else {
            RidingHud.drawMountHearts(canvas, mount, canvas.width() / 2 + 91, canvas.height() - gui.rightHeight, false);
            gui.rightHeight += rows;
        }
    }
}
