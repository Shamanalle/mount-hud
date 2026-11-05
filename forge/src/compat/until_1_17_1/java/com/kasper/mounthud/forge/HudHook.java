package com.kasper.mounthud.forge;

import com.kasper.mounthud.client.RidingHud;
import com.kasper.mounthud.compat.Canvas;
import com.kasper.mounthud.compat.Mounts;
import com.kasper.mounthud.hud.HudPlan;
import com.kasper.mounthud.hud.MountHearts;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Forge HUD overlay listener (Minecraft 1.17.1: RenderGameOverlayEvent.PreLayer / PostLayer and GuiContainerEvent).
 */
public final class HudHook {
    private HudPlan plan;

    @SubscribeEvent
    public void pre(RenderGameOverlayEvent.PreLayer event) {
        IIngameOverlay overlay = event.getOverlay();
        if (overlay == ForgeIngameGui.PLAYER_HEALTH_ELEMENT) {
            plan = RidingHud.plan();
        }
        if (plan == null || plan.vanilla) {
            return;
        }
        if (overlay == ForgeIngameGui.MOUNT_HEALTH_ELEMENT) {
            if (RidingHud.drawsMountHearts(plan)) {
                drawMountHud(event, true);
                event.setCanceled(true);
            }
        } else if (overlay == ForgeIngameGui.JUMP_BAR_ELEMENT) {
            if (RidingHud.hidesJumpBar(plan)) {
                event.setCanceled(true);
            }
        } else if (overlay == ForgeIngameGui.EXPERIENCE_BAR_ELEMENT) {
            Minecraft minecraft = Minecraft.getInstance();
            if (plan.bar == HudPlan.Bar.EXPERIENCE && Mounts.isJumpingMount(minecraft.player)
                    && !minecraft.options.hideGui && minecraft.gui instanceof ForgeIngameGui) {
                ForgeIngameGui gui = (ForgeIngameGui) minecraft.gui;
                gui.setupOverlayRenderState(true, false);
                RenderSystem.disableBlend();
                gui.renderExperienceBar(event.getMatrixStack(), event.getWindow().getGuiScaledWidth() / 2 - 91);
                RenderSystem.enableBlend();
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void postFood(RenderGameOverlayEvent.PostLayer event) {
        if (plan == null || plan.vanilla || !RidingHud.drawsMountHearts(plan)
                || event.getOverlay() != ForgeIngameGui.FOOD_LEVEL_ELEMENT) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (RidingHud.mount() == null || minecraft.options.hideGui || !(minecraft.gui instanceof ForgeIngameGui)) {
            return;
        }
        ForgeIngameGui gui = (ForgeIngameGui) minecraft.gui;
        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();
        gui.setupOverlayRenderState(true, false);
        gui.renderFood(width, height, event.getMatrixStack());
    }

    @SubscribeEvent
    public void postMountHealth(RenderGameOverlayEvent.PostLayer event) {
        if (plan == null || plan.vanilla || RidingHud.drawsMountHearts(plan) || !plan.stats
                || event.getOverlay() != ForgeIngameGui.MOUNT_HEALTH_ELEMENT) {
            return;
        }
        drawMountHud(event, false);
    }

    @SubscribeEvent
    public void containerForeground(GuiContainerEvent.DrawForeground event) {
        RidingHud.drawInventoryStats(new Canvas(event.getMatrixStack()), event.getGuiContainer());
    }

    private void drawMountHud(RenderGameOverlayEvent event, boolean drawHearts) {
        Minecraft minecraft = Minecraft.getInstance();
        LivingEntity mount = RidingHud.mount();
        if (mount == null || minecraft.options.hideGui || !(minecraft.gui instanceof ForgeIngameGui)) {
            return;
        }
        ForgeIngameGui gui = (ForgeIngameGui) minecraft.gui;
        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();
        gui.setupOverlayRenderState(true, false);
        Canvas canvas = new Canvas(event.getMatrixStack());
        int rows = MountHearts.rows(MountHearts.hearts(mount.getMaxHealth()));
        boolean left = plan.hearts == HudPlan.Hearts.LEFT;
        if (drawHearts) {
            if (left) {
                RidingHud.drawMountHearts(canvas, mount, width / 2 - 91, height - gui.left_height, true);
                gui.left_height += rows * MountHearts.ROW_HEIGHT;
            } else {
                RidingHud.drawMountHearts(canvas, mount, width / 2 + 91, height - gui.right_height, false);
                gui.right_height += rows * MountHearts.ROW_HEIGHT;
            }
        }
        if (plan.stats) {
            if (left) {
                RidingHud.drawHudStats(canvas, mount, width / 2 - 91, height - gui.left_height, true);
                gui.left_height += MountHearts.ROW_HEIGHT;
            } else {
                RidingHud.drawHudStats(canvas, mount, width / 2 + 91, height - gui.right_height, false);
                gui.right_height += MountHearts.ROW_HEIGHT;
            }
        }
    }
}
