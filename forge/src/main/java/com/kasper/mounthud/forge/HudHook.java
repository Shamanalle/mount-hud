package com.kasper.mounthud.forge;

import com.kasper.mounthud.client.RidingHud;
import com.kasper.mounthud.compat.Canvas;
import com.kasper.mounthud.hud.HudPlan;
import com.kasper.mounthud.hud.MountHearts;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.NamedGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Forge draws the HUD as named overlays, each stacking its rows on ForgeGui's leftHeight and rightHeight. Hunger is
 * drawn by the mod after the hunger overlay (which skips it on a mount), the mount's hearts right after it (on the
 * right) or on the left, and the vanilla mount health overlay is cancelled; the air overlay that follows stacks above
 * them on its own.
 */
public final class HudHook {
    private HudPlan plan;

    @SubscribeEvent
    public void pre(RenderGuiOverlayEvent.Pre event) {
        NamedGuiOverlay overlay = event.getOverlay();
        if (overlay.id().equals(VanillaGuiOverlay.PLAYER_HEALTH.id())) {
            // The first overlay of the hotbar decorations: the plan for this frame
            plan = RidingHud.plan();
        }
        if (plan == null || plan.vanilla) {
            return;
        }
        if (overlay.id().equals(VanillaGuiOverlay.MOUNT_HEALTH.id())) {
            if (RidingHud.drawsMountHearts(plan)) {
                event.setCanceled(true);
            }
        } else if (overlay.id().equals(VanillaGuiOverlay.JUMP_BAR.id())) {
            if (RidingHud.hidesJumpBar(plan)) {
                event.setCanceled(true);
            }
        } else if (overlay.id().equals(VanillaGuiOverlay.EXPERIENCE_BAR.id())) {
            Minecraft minecraft = Minecraft.getInstance();
            if (plan.bar == HudPlan.Bar.EXPERIENCE && minecraft.player.jumpableVehicle() != null
                    && !minecraft.options.hideGui && minecraft.gui instanceof ForgeGui) {
                // The overlay skips experience on a mount that jumps: draw it as it would
                ForgeGui gui = (ForgeGui) minecraft.gui;
                gui.setupOverlayRenderState(true, false);
                RenderSystem.disableBlend();
                gui.renderExperienceBar(event.getGuiGraphics(), event.getWindow().getGuiScaledWidth() / 2 - 91);
                RenderSystem.enableBlend();
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void post(RenderGuiOverlayEvent.Post event) {
        if (plan == null || plan.vanilla || !RidingHud.drawsMountHearts(plan)
                || !event.getOverlay().id().equals(VanillaGuiOverlay.FOOD_LEVEL.id())) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        LivingEntity mount = RidingHud.mount();
        if (mount == null || minecraft.options.hideGui || !(minecraft.gui instanceof ForgeGui)) {
            return;
        }
        ForgeGui gui = (ForgeGui) minecraft.gui;
        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();
        gui.setupOverlayRenderState(true, false);
        // The hunger overlay skips hunger on a mount with health
        gui.renderFood(width, height, event.getGuiGraphics());
        Canvas canvas = new Canvas(event.getGuiGraphics());
        int rows = MountHearts.rows(MountHearts.hearts(mount.getMaxHealth()));
        if (plan.hearts == HudPlan.Hearts.LEFT) {
            RidingHud.drawMountHearts(canvas, mount, width / 2 - 91, height - gui.leftHeight, true);
            gui.leftHeight += rows * MountHearts.ROW_HEIGHT;
        } else {
            RidingHud.drawMountHearts(canvas, mount, width / 2 + 91, height - gui.rightHeight, false);
            gui.rightHeight += rows * MountHearts.ROW_HEIGHT;
        }
    }
}
