package com.kasper.mounthud.gametest;

import com.kasper.mounthud.MountHud;
import com.kasper.mounthud.client.SettingsScreen;
import com.kasper.mounthud.config.HeartsPosition;
import com.kasper.mounthud.config.JumpBarMode;
import com.kasper.mounthud.config.MountStatsMode;
import com.kasper.mounthud.config.Settings;
import com.kasper.mounthud.hud.HudLayout;
import com.kasper.mounthud.hud.MountHearts;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.client.resources.language.I18n;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Puts the player on a horse and checks the riding HUD: the mount's hearts above hunger or on the left, the jump
 * bar while charging a jump, the mount's stats in the horse inventory and on the HUD, and the settings screen. Every
 * step leaves a screenshot in build/run/clientGameTest/screenshots; the ones named store_* are 3840x2160, for the
 * project pages.
 */
public final class MountHudGameTest implements FabricClientGameTest {
    private static final Logger LOGGER = LoggerFactory.getLogger("MountHud/GameTest");
    /** Every translation but English and Russian: the settings screen only, to catch strings that do not fit. */
    private static final List<String> TRANSLATIONS = List.of(
            "de_de", "es_es", "fr_fr", "it_it", "ja_jp", "ko_kr", "pl_pl", "pt_br", "tr_tr", "uk_ua", "zh_cn");
    /** The horse has 30 health (two rows of mount hearts), 0.225 speed (9.7 bl/s) and 0.7 jump strength (2.9 bl). */
    private static final String HORSE = "minecraft:horse 0.5 -60 2.5 {Tame:1b,Variant:3,Rotation:[0f,0f],Health:23f,"
            + "attributes:[{id:\"minecraft:max_health\",base:30d},"
            + "{id:\"minecraft:movement_speed\",base:0.225d},"
            + "{id:\"minecraft:jump_strength\",base:0.7d}],"
            // The saddle is an equipment slot from 1.21.5 on and SaddleItem before; each version skips the other
            + "SaddleItem:{id:\"minecraft:saddle\",count:1},equipment:{saddle:{id:\"minecraft:saddle\",count:1}}}";
    private static final int STORE_GUI_SCALE = 6;

    private final List<String> failures = new ArrayList<>();

    @Override
    public void runTest(ClientGameTestContext context) {
        // A slow frame slows the world load on a software-rendered runner; keep the view small
        context.runOnClient(mc -> {
            mc.options.menuBackgroundBlurriness().set(0);
            mc.options.renderDistance().set(4);
            mc.options.simulationDistance().set(5);
        });

        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            TestServerContext server = world.getServer();
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            server.runCommand("gamemode survival @a");
            server.runCommand("tp @a 0.5 -60 0.5 0 0");
            TestCompat.waitForChunks(world);
            mount(context, server);

            check(context, "the mount's hearts go above hunger", () -> aboveHunger(context, "en_"));
            check(context, "the mount's stats show in the horse inventory and on the HUD", () -> mountStats(context, "en_"));
            check(context, "the mount's hearts go left, above armor", () -> left(context, server, "en_"));
            check(context, "the jump bar shows while charging a jump", () -> jumpBar(context, "en_"));
            check(context, "the settings screen resets the settings", () -> settingsScreen(context, "en"));

            // Russian strings are longer than English ones
            setLanguage(context, "ru_ru");
            check(context, "Russian: HUD, inventory stats and settings", () -> {
                aboveHunger(context, "ru_");
                mountStats(context, "ru_");
                settingsScreen(context, "ru");
            });
            for (String code : TRANSLATIONS) {
                setLanguage(context, code);
                context.setScreen(() -> new SettingsScreen(null));
                context.takeScreenshot("lang_" + code + "_settings");
            }
            context.setScreen(() -> null);

            setLanguage(context, "en_us");
            check(context, "store screenshots", () -> storeScreenshots(context, server));
        }

        if (!failures.isEmpty()) {
            throw new AssertionError(failures.size() + " Mount HUD check(s) failed:\n - " + String.join("\n - ", failures));
        }
        LOGGER.info("Mount HUD game tests passed");
    }

    // --- Checks ---------------------------------------------------------------------------------

    private void aboveHunger(ClientGameTestContext context, String prefix) {
        apply(context, s -> s.heartsPosition = HeartsPosition.ABOVE_HUNGER);
        BufferedImage modded = shot(context, prefix + "above_hunger");
        Region row = context.computeOnClient(mc -> Region.rightRow(mc, HudLayout.aboveHungerRow(height(mc))));
        Region hunger = context.computeOnClient(mc -> Region.rightRow(mc, height(mc) - HudLayout.BOTTOM_ROW));
        apply(context, s -> s.enabled = false);
        BufferedImage vanilla = shot(context, prefix + "vanilla");
        expect(row.hearts(modded) > row.minHearts(), "no mount hearts above hunger (" + row.hearts(modded) + " heart pixels)");
        expect(hunger.hearts(modded) < hunger.minHearts(), "the mount's hearts still cover hunger");
        // Vanilla draws the mount's hearts in place of hunger: if the check does not see them there, it looks at the
        // wrong place or for the wrong colour
        expect(hunger.hearts(vanilla) > hunger.minHearts(), "no mount hearts in place of hunger on the vanilla HUD, the check is off");
    }

    private void left(ClientGameTestContext context, TestServerContext server, String prefix) {
        server.runCommand("item replace entity @a armor.chest with minecraft:iron_chestplate");
        server.runCommand("item replace entity @a armor.legs with minecraft:iron_leggings");
        server.runCommand("item replace entity @a armor.feet with minecraft:iron_boots");
        context.waitTicks(3);
        apply(context, s -> s.heartsPosition = HeartsPosition.LEFT);
        BufferedImage modded = shot(context, prefix + "left");
        Region row = context.computeOnClient(mc -> Region.leftRow(mc,
                HudLayout.leftFreeRow(height(mc), mc.player.getMaxHealth(), mc.player.getAbsorptionAmount(),
                        mc.player.getArmorValue())));
        apply(context, s -> s.enabled = false);
        BufferedImage vanilla = shot(context, prefix + "left_vanilla");
        server.runCommand("item replace entity @a armor.chest with minecraft:air");
        server.runCommand("item replace entity @a armor.legs with minecraft:air");
        server.runCommand("item replace entity @a armor.feet with minecraft:air");
        expect(row.hearts(modded) > row.minHearts(), "no mount hearts above the armor (" + row.hearts(modded) + " heart pixels)");
        expect(row.hearts(vanilla) < row.minHearts(), "the vanilla HUD has hearts above the armor, the check looks at the wrong place");
    }

    private void jumpBar(ClientGameTestContext context, String prefix) {
        apply(context, s -> s.jumpBar = JumpBarMode.AUTO);
        shot(context, prefix + "auto_experience");
        context.getInput().holdKey(options -> options.keyJump);
        context.waitTicks(6);
        float charge = context.computeOnClient(mc -> mc.player.getJumpRidingScale());
        shot(context, prefix + "auto_jump_charging");
        context.getInput().releaseKey(options -> options.keyJump);
        context.waitTicks(30);
        expect(charge > 0, "holding jump on the horse did not charge a jump");

        apply(context, s -> s.jumpBar = JumpBarMode.ALWAYS_JUMP);
        shot(context, prefix + "always_jump");
        apply(context, s -> s.jumpBar = JumpBarMode.ALWAYS_EXPERIENCE);
        shot(context, prefix + "always_experience");
    }

    private void mountStats(ClientGameTestContext context, String prefix) {
        // 1. HUD stats above the two rows of mount hearts
        apply(context, s -> s.mountStats = MountStatsMode.INVENTORY);
        BufferedImage hudWithoutStats = shot(context, prefix + "hud_no_stats");
        apply(context, s -> s.mountStats = MountStatsMode.HUD_AND_INVENTORY);
        BufferedImage hudWithStats = shot(context, prefix + "hud_stats");
        Region statsRow = context.computeOnClient(mc -> Region.rightRow(mc,
                HudLayout.aboveHungerRow(height(mc)) - 2 * MountHearts.ROW_HEIGHT));
        expect(statsRow.whiteText(hudWithStats) > statsRow.minText(),
                "no mount stats text above mount hearts on the HUD (" + statsRow.whiteText(hudWithStats) + " white pixels)");
        expect(statsRow.whiteText(hudWithoutStats) < statsRow.minText(),
                "HUD has white text pixels above mount hearts when HUD stats are off");

        // 2. Horse inventory screen stats in the right-hand panel
        apply(context, s -> s.mountStats = MountStatsMode.INVENTORY);
        context.runOnClient(mc -> mc.player.sendOpenInventory());
        context.waitFor(mc -> TestCompat.screen(mc) instanceof HorseInventoryScreen, 200);
        context.waitTicks(2);
        BufferedImage invWithStats = shot(context, prefix + "horse_inventory");
        Region panel = context.computeOnClient(Region::horseStatsPanel);
        apply(context, s -> s.mountStats = MountStatsMode.OFF);
        BufferedImage invWithoutStats = shot(context, prefix + "horse_inventory_off");
        context.runOnClient(mc -> mc.player.closeContainer());
        context.waitTicks(2);
        expect(panel.darkText(invWithStats) > panel.minText(),
                "no mount stats in the horse inventory panel (" + panel.darkText(invWithStats) + " dark pixels)");
        expect(panel.darkText(invWithoutStats) == 0,
                "horse inventory panel still has dark pixels when mount stats are off");
    }

    private void settingsScreen(ClientGameTestContext context, String lang) {
        apply(context, s -> {
            s.heartsPosition = HeartsPosition.LEFT;
            s.jumpBar = JumpBarMode.ALWAYS_JUMP;
            s.mountStats = MountStatsMode.HUD_AND_INVENTORY;
        });
        context.setScreen(() -> new SettingsScreen(null));
        context.takeScreenshot(lang + "_settings_changed");
        context.clickScreenButton(context.computeOnClient(mc -> I18n.get("mounthud.settings.reset")));
        context.waitTicks(2);
        context.takeScreenshot(lang + "_settings");
        context.clickScreenButton(context.computeOnClient(mc -> I18n.get("gui.done")));
        context.waitTicks(2);
        expect(context.computeOnClient(mc -> TestCompat.screen(mc) == null), "Done did not close the settings screen");
        expect(MountHud.settings().equals(new Settings()), "Reset did not restore the default settings");
    }

    /** The pictures for the project pages, 3840x2160 at a large GUI scale so the HUD reads at a glance. */
    private void storeScreenshots(ClientGameTestContext context, TestServerContext server) {
        context.runOnClient(mc -> mc.options.renderDistance().set(8));
        buildStoreScene(server);
        context.getInput().resizeWindow(3840, 2160);
        context.runOnClient(mc -> {
            TestCompat.guiScale(mc, STORE_GUI_SCALE);
            mc.player.setYRot(0);
            mc.player.setXRot(8);
        });
        context.waitTicks(20);

        apply(context, s -> {
            s.heartsPosition = HeartsPosition.ABOVE_HUNGER;
            s.mountStats = MountStatsMode.HUD_AND_INVENTORY;
        });
        shot(context, "store_above_hunger");

        context.getInput().holdKey(options -> options.keyJump);
        context.waitTicks(8);
        shot(context, "store_jump_bar");
        context.getInput().releaseKey(options -> options.keyJump);
        context.waitTicks(30);

        context.runOnClient(mc -> {
            mc.player.setYRot(0);
            mc.player.setXRot(8);
        });
        context.waitTicks(2);
        apply(context, s -> s.heartsPosition = HeartsPosition.LEFT);
        shot(context, "store_left");

        apply(context, s -> s.enabled = false);
        shot(context, "store_vanilla");

        server.runCommand("item replace entity @e[type=minecraft:horse,limit=1] armor.body with minecraft:diamond_horse_armor");
        apply(context, s -> { });
        context.runOnClient(mc -> mc.player.sendOpenInventory());
        context.waitFor(mc -> TestCompat.screen(mc) instanceof HorseInventoryScreen, 200);
        context.waitTicks(3);
        context.takeScreenshot("store_inventory");
        context.runOnClient(mc -> mc.player.closeContainer());
        server.runCommand("item replace entity @e[type=minecraft:horse,limit=1] armor.body with minecraft:air");
        context.waitTicks(3);

        context.setScreen(() -> new SettingsScreen(null));
        context.takeScreenshot("store_settings");
        context.setScreen(() -> null);
        context.runOnClient(mc -> TestCompat.guiScale(mc, 0));
        context.getInput().resizeWindow(854, 480);
        context.waitTicks(2);
    }

    /** Builds a countryside road, paddock, pond, trees and hills in front of the player for store screenshots. */
    private static void buildStoreScene(TestServerContext server) {
        server.runCommand("item replace entity @a armor.chest with minecraft:iron_chestplate");
        server.runCommand("item replace entity @a armor.legs with minecraft:iron_leggings");
        server.runCommand("item replace entity @a armor.feet with minecraft:iron_boots");

        // Rolling hills on the horizon (z = 22..42) with staggered grass edges so the skyline looks organic
        server.runCommand("fill 4 -60 23 26 -60 39 minecraft:grass_block");
        server.runCommand("fill 7 -60 21 19 -60 23 minecraft:grass_block");
        server.runCommand("fill 6 -59 25 24 -59 37 minecraft:grass_block");
        server.runCommand("fill 9 -59 23 17 -59 25 minecraft:grass_block");
        server.runCommand("fill 9 -58 27 21 -58 35 minecraft:grass_block");

        server.runCommand("fill -26 -60 23 -4 -60 39 minecraft:grass_block");
        server.runCommand("fill -18 -60 21 -7 -60 23 minecraft:grass_block");
        server.runCommand("fill -24 -59 25 -6 -59 37 minecraft:grass_block");
        server.runCommand("fill -16 -59 23 -9 -59 25 minecraft:grass_block");
        server.runCommand("fill -20 -58 27 -9 -58 35 minecraft:grass_block");

        server.runCommand("fill -6 -60 27 6 -60 40 minecraft:grass_block");
        server.runCommand("fill -4 -59 30 4 -59 38 minecraft:grass_block");

        // Winding country road heading South (+Z)
        server.runCommand("fill -1 -61 0 1 -61 15 minecraft:dirt_path");
        server.runCommand("fill -2 -61 13 0 -61 26 minecraft:dirt_path");
        server.runCommand("setblock 0 -61 7 minecraft:coarse_dirt");
        server.runCommand("setblock -1 -61 10 minecraft:coarse_dirt");
        server.runCommand("setblock 1 -61 12 minecraft:mossy_cobblestone");
        server.runCommand("setblock -1 -61 16 minecraft:coarse_dirt");
        server.runCommand("setblock -2 -61 20 minecraft:mossy_cobblestone");

        // Left side (+X): oak fence paddock with hay bales, water trough and a saddled camel
        server.runCommand("fill 4 -60 7 11 -60 15 minecraft:oak_fence replace minecraft:air");
        server.runCommand("fill 5 -60 8 10 -60 14 minecraft:air");
        server.runCommand("setblock 4 -60 10 minecraft:oak_fence_gate[facing=west,open=false]");
        server.runCommand("setblock 4 -59 7 minecraft:lantern");
        server.runCommand("setblock 4 -59 15 minecraft:lantern");
        server.runCommand("setblock 11 -59 7 minecraft:lantern");
        server.runCommand("setblock 5 -60 8 minecraft:water_cauldron[level=3]");
        server.runCommand("setblock 5 -60 9 minecraft:water_cauldron[level=3]");
        server.runCommand("setblock 9 -60 13 minecraft:hay_block[axis=y]");
        server.runCommand("setblock 10 -60 13 minecraft:hay_block[axis=x]");
        server.runCommand("setblock 10 -59 13 minecraft:hay_block[axis=y]");
        server.runCommand("setblock 10 -60 14 minecraft:hay_block[axis=z]");
        server.runCommand("summon minecraft:camel 7.5 -60 11.2 {NoAI:1b,Tame:1b,Rotation:[140f,4f],"
                + "SaddleItem:{id:\"minecraft:saddle\",count:1},equipment:{saddle:{id:\"minecraft:saddle\",count:1}}}");

        // Right side (-X): roadside lamp post, small pond with sugar cane, barrel, hay bales and a second horse
        server.runCommand("fill -3 -60 8 -3 -58 8 minecraft:oak_fence");
        server.runCommand("setblock -2 -58 8 minecraft:oak_fence");
        server.runCommand("setblock -2 -59 8 minecraft:lantern[hanging=true]");
        server.runCommand("fill -8 -61 9 -5 -61 13 minecraft:water");
        server.runCommand("setblock -6 -60 10 minecraft:lily_pad");
        server.runCommand("setblock -7 -60 12 minecraft:lily_pad");
        server.runCommand("setblock -8 -61 8 minecraft:mossy_cobblestone");
        server.runCommand("setblock -5 -61 9 minecraft:coarse_dirt");
        server.runCommand("setblock -9 -61 10 minecraft:sand");
        server.runCommand("fill -9 -60 10 -9 -59 10 minecraft:sugar_cane");
        server.runCommand("fill -9 -60 11 -9 -58 11 minecraft:sugar_cane");
        server.runCommand("setblock -4 -60 15 minecraft:barrel[facing=up]");
        server.runCommand("setblock -5 -60 15 minecraft:hay_block[axis=y]");
        server.runCommand("setblock -5 -60 16 minecraft:hay_block[axis=z]");
        server.runCommand("summon minecraft:horse -4.2 -60 12.8 {NoAI:1b,Tame:1b,Variant:258,Rotation:[-145f,4f],"
                + "SaddleItem:{id:\"minecraft:saddle\",count:1},equipment:{saddle:{id:\"minecraft:saddle\",count:1},"
                + "body:{id:\"minecraft:golden_horse_armor\",count:1}}}");

        // Trees framing the scene on both sides and on the background hills
        plantTree(server, 13, -60, 13, "minecraft:oak_log", "minecraft:oak_leaves[persistent=true]");
        plantTree(server, -11, -60, 14, "minecraft:birch_log", "minecraft:birch_leaves[persistent=true]");
        plantTree(server, 9, -57, 28, "minecraft:oak_log", "minecraft:oak_leaves[persistent=true]");
        plantTree(server, 17, -58, 25, "minecraft:birch_log", "minecraft:birch_leaves[persistent=true]");
        plantTree(server, -11, -58, 28, "minecraft:oak_log", "minecraft:oak_leaves[persistent=true]");
        plantTree(server, -18, -59, 24, "minecraft:oak_log", "minecraft:oak_leaves[persistent=true]");
        plantTree(server, 2, -58, 33, "minecraft:birch_log", "minecraft:birch_leaves[persistent=true]");

        // Wildflowers and meadow grass
        int[][] grassSpots = {
                {3, 5}, {2, 9}, {3, 12}, {2, 16}, {4, 18}, {6, 19}, {8, 18},
                {-3, 6}, {-4, 7}, {-3, 11}, {-3, 18}, {-6, 17}, {-8, 16}
        };
        for (int[] g : grassSpots) {
            server.runCommand("setblock " + g[0] + " -60 " + g[1] + " minecraft:short_grass");
        }
        server.runCommand("setblock 3 -60 6 minecraft:dandelion");
        server.runCommand("setblock 2 -60 11 minecraft:poppy");
        server.runCommand("setblock 5 -60 17 minecraft:cornflower");
        server.runCommand("setblock -4 -60 6 minecraft:oxeye_daisy");
        server.runCommand("setblock -3 -60 14 minecraft:dandelion");
        server.runCommand("setblock -6 -60 15 minecraft:poppy");
    }

    private static void plantTree(TestServerContext server, int x, int y, int z, String log, String leaves) {
        server.runCommand("fill " + (x - 2) + " " + (y + 3) + " " + (z - 2) + " " + (x + 2) + " " + (y + 4) + " " + (z + 2) + " " + leaves);
        server.runCommand("setblock " + (x - 2) + " " + (y + 4) + " " + (z - 2) + " minecraft:air");
        server.runCommand("setblock " + (x + 2) + " " + (y + 4) + " " + (z - 2) + " minecraft:air");
        server.runCommand("setblock " + (x - 2) + " " + (y + 4) + " " + (z + 2) + " minecraft:air");
        server.runCommand("setblock " + (x + 2) + " " + (y + 4) + " " + (z + 2) + " minecraft:air");
        server.runCommand("fill " + (x - 1) + " " + (y + 5) + " " + (z - 1) + " " + (x + 1) + " " + (y + 5) + " " + (z + 1) + " " + leaves);
        server.runCommand("setblock " + (x - 1) + " " + (y + 5) + " " + (z - 1) + " minecraft:air");
        server.runCommand("setblock " + (x + 1) + " " + (y + 5) + " " + (z - 1) + " minecraft:air");
        server.runCommand("fill " + (x - 1) + " " + (y + 6) + " " + z + " " + (x + 1) + " " + (y + 6) + " " + z + " " + leaves);
        server.runCommand("fill " + x + " " + (y + 6) + " " + (z - 1) + " " + x + " " + (y + 6) + " " + (z + 1) + " " + leaves);
        server.runCommand("fill " + x + " " + y + " " + z + " " + x + " " + (y + 4) + " " + z + " " + log);
    }

    // --- Helpers --------------------------------------------------------------------------------

    /** A tame, saddled horse with the player on it, the player hungry and with some experience. */
    private static void mount(ClientGameTestContext context, TestServerContext server) {
        server.runCommand("summon " + HORSE);
        server.runCommand("ride @p mount @e[type=minecraft:horse,limit=1]");
        server.runCommand("xp set @a 7 levels");
        server.runCommand("xp add @a 20 points");
        server.runCommand("effect give @a minecraft:hunger 2 200 true");
        // Populate hotbar slots 1..8 and inventory early so recipe unlock toasts finish before screenshots
        server.runCommand("item replace entity @a hotbar.1 with minecraft:diamond_sword");
        server.runCommand("item replace entity @a hotbar.2 with minecraft:bow");
        server.runCommand("item replace entity @a hotbar.3 with minecraft:golden_carrot 14");
        server.runCommand("item replace entity @a hotbar.4 with minecraft:cooked_beef 24");
        server.runCommand("item replace entity @a hotbar.5 with minecraft:lead 2");
        server.runCommand("item replace entity @a hotbar.6 with minecraft:apple 8");
        server.runCommand("item replace entity @a hotbar.7 with minecraft:saddle");
        server.runCommand("item replace entity @a hotbar.8 with minecraft:lantern 4");
        server.runCommand("item replace entity @a inventory.0 with minecraft:arrow 48");
        server.runCommand("item replace entity @a inventory.1 with minecraft:wheat 19");
        server.runCommand("item replace entity @a inventory.2 with minecraft:oak_planks 32");
        context.waitFor(mc -> mc.player.getVehicle() != null, 200);
        context.waitTicks(80);
        // Look over the horse's head, so the HUD stands on the sky
        context.runOnClient(mc -> {
            mc.player.setYRot(0);
            mc.player.setXRot(-20);
        });
    }

    private void check(ClientGameTestContext context, String name, Runnable body) {
        LOGGER.info("[RUN ] {}", name);
        try {
            body.run();
            LOGGER.info("[PASS] {}", name);
        } catch (Throwable t) {
            failures.add(name + ": " + t.getMessage());
            LOGGER.error("[FAIL] {}", name, t);
            context.takeScreenshot("fail_" + name.replaceAll("[^a-zA-Z]+", "_"));
        } finally {
            context.setScreen(() -> null);
            context.runOnClient(mc -> MountHud.apply(new Settings()));
        }
    }

    /** The default settings, changed by {@code change}. */
    private static void apply(ClientGameTestContext context, Consumer<Settings> change) {
        context.runOnClient(mc -> {
            Settings settings = new Settings();
            change.accept(settings);
            MountHud.apply(settings);
        });
        context.waitTicks(2);
    }

    private static BufferedImage shot(ClientGameTestContext context, String name) {
        context.runOnClient(TestCompat::clearChat);
        context.waitTicks(1);
        Path file = context.takeScreenshot(name);
        try {
            return ImageIO.read(file.toFile());
        } catch (IOException e) {
            throw new AssertionError("could not read the screenshot " + file, e);
        }
    }

    private static void setLanguage(ClientGameTestContext context, String code) {
        context.runOnClient(mc -> {
            mc.options.languageCode = code;
            mc.getLanguageManager().setSelected(code);
            mc.reloadResourcePacks();
        });
        context.waitFor(TestCompat::overlayGone, 1200);
        context.waitTicks(2);
    }

    private static int height(net.minecraft.client.Minecraft mc) {
        return mc.getWindow().getGuiScaledHeight();
    }

    private static void expect(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    /** A rectangle on the HUD or inside the horse inventory screen, in screenshot pixels. */
    private record Region(int x, int y, int width, int height, int scale) {
        static Region rightRow(net.minecraft.client.Minecraft mc, int row) {
            int center = mc.getWindow().getGuiScaledWidth() / 2;
            return of(mc, center + 11, row, 80, 9);
        }

        static Region leftRow(net.minecraft.client.Minecraft mc, int row) {
            int center = mc.getWindow().getGuiScaledWidth() / 2;
            return of(mc, center - 91, row, 80, 9);
        }

        static Region horseStatsPanel(net.minecraft.client.Minecraft mc) {
            int leftPos = (mc.getWindow().getGuiScaledWidth() - 176) / 2;
            int topPos = (mc.getWindow().getGuiScaledHeight() - 166) / 2;
            return of(mc, leftPos + 83, topPos + 21, 52, 36);
        }

        private static Region of(net.minecraft.client.Minecraft mc, int x, int y, int width, int height) {
            int scale = (int) Math.round(mc.getWindow().getGuiScale());
            return new Region(x * scale, y * scale, width * scale, height * scale, scale);
        }

        /** Fewer heart pixels than a few hearts have: no hearts in the row. */
        int minHearts() {
            return 40 * scale * scale;
        }

        /** Minimum glyph pixels for a line of stats text. */
        int minText() {
            return 25 * scale * scale;
        }

        /**
         * The pixels of the mount's hearts in the row: they are orange, unlike the player's red hearts and the brown
         * and green food.
         */
        int hearts(BufferedImage image) {
            int count = 0;
            for (int py = y; py < Math.min(y + height, image.getHeight()); py++) {
                for (int px = x; px < Math.min(x + width, image.getWidth()); px++) {
                    int rgb = image.getRGB(px, py);
                    int r = rgb >> 16 & 0xFF, g = rgb >> 8 & 0xFF, b = rgb & 0xFF;
                    if (r > 180 && g > 70 && g < 140 && b < 80) {
                        count++;
                    }
                }
            }
            return count;
        }

        /** Bright white text pixels (0xFFFFFF) drawn on the HUD. */
        int whiteText(BufferedImage image) {
            int count = 0;
            for (int py = y; py < Math.min(y + height, image.getHeight()); py++) {
                for (int px = x; px < Math.min(x + width, image.getWidth()); px++) {
                    int rgb = image.getRGB(px, py);
                    int r = rgb >> 16 & 0xFF, g = rgb >> 8 & 0xFF, b = rgb & 0xFF;
                    if (r > 230 && g > 230 && b > 230) {
                        count++;
                    }
                }
            }
            return count;
        }

        /** Dark gray text pixels (0x404040) drawn on the light gray (0xC6C6C6) horse inventory panel. */
        int darkText(BufferedImage image) {
            int count = 0;
            for (int py = y; py < Math.min(y + height, image.getHeight()); py++) {
                for (int px = x; px < Math.min(x + width, image.getWidth()); px++) {
                    int rgb = image.getRGB(px, py);
                    int r = rgb >> 16 & 0xFF, g = rgb >> 8 & 0xFF, b = rgb & 0xFF;
                    if (r < 90 && g < 90 && b < 90) {
                        count++;
                    }
                }
            }
            return count;
        }
    }
}
