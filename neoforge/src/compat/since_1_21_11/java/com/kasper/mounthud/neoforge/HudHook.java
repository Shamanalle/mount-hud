package com.kasper.mounthud.neoforge;

import com.kasper.mounthud.MountHud;
import com.kasper.mounthud.client.RidingHud;
import com.kasper.mounthud.compat.Canvas;
import com.kasper.mounthud.hud.HudPlan;
import com.kasper.mounthud.hud.MountHearts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * NeoForge draws the HUD as named layers (VanillaGuiLayers), each stacking its rows on Gui's leftHeight and
 * rightHeight. Our layer is registered directly above VEHICLE_HEALTH through RegisterGuiLayersEvent so the mount's
 * hearts and optional stats are drawn above hunger on the right (where the air layer that follows stacks above them)
 * or above armor on the left (Minecraft 1.21.11 – 26.1.x, where ResourceLocation was renamed to Identifier).
 */
public final class HudHook {
    private static final Identifier LAYER_ID =
            Identifier.fromNamespaceAndPath(MountHud.MOD_ID, "vehicle_health");

    private HudHook() {
    }

    public static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.VEHICLE_HEALTH, LAYER_ID, (graphics, deltaTracker) -> {
            HudPlan plan = RidingHud.plan();
            if (RidingHud.drawsMountHearts(plan) || plan.stats) {
                drawMountHud(new Canvas(graphics), plan);
            }
        });
    }

    private static void drawMountHud(Canvas canvas, HudPlan plan) {
        Minecraft minecraft = Minecraft.getInstance();
        LivingEntity mount = RidingHud.mount();
        if (mount == null || minecraft.options.hideGui) {
            return;
        }
        Gui gui = minecraft.gui;
        int rows = plan.heartRows * MountHearts.ROW_HEIGHT;
        boolean left = plan.hearts == HudPlan.Hearts.LEFT;
        if (RidingHud.drawsMountHearts(plan)) {
            if (left) {
                RidingHud.drawMountHearts(canvas, mount, canvas.width() / 2 - 91, canvas.height() - gui.leftHeight, true);
                gui.leftHeight += rows;
            } else {
                RidingHud.drawMountHearts(canvas, mount, canvas.width() / 2 + 91, canvas.height() - gui.rightHeight, false);
                gui.rightHeight += rows;
            }
        }
        if (plan.stats) {
            if (left) {
                RidingHud.drawHudStats(canvas, mount, canvas.width() / 2 - 91, canvas.height() - gui.leftHeight, true);
                gui.leftHeight += MountHearts.ROW_HEIGHT;
            } else {
                RidingHud.drawHudStats(canvas, mount, canvas.width() / 2 + 91, canvas.height() - gui.rightHeight, false);
                gui.rightHeight += MountHearts.ROW_HEIGHT;
            }
        }
    }
}
