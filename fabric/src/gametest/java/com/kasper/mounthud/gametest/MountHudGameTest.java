package com.kasper.mounthud.gametest;

import com.kasper.mounthud.MountHud;
import com.kasper.mounthud.client.SettingsScreen;
import com.kasper.mounthud.config.HeartsPosition;
import com.kasper.mounthud.config.JumpBarMode;
import com.kasper.mounthud.config.Settings;
import com.kasper.mounthud.hud.HudLayout;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
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
 * bar while charging a jump, the settings screen. Every step leaves a screenshot in
 * build/run/clientGameTest/screenshots; the ones named store_* are 3840x2160, for the project pages.
 */
public final class MountHudGameTest implements FabricClientGameTest {
    private static final Logger LOGGER = LoggerFactory.getLogger("MountHud/GameTest");
    /** Every translation but English and Russian: the settings screen only, to catch strings that do not fit. */
    private static final List<String> TRANSLATIONS = List.of(
            "de_de", "es_es", "fr_fr", "it_it", "ja_jp", "ko_kr", "pl_pl", "pt_br", "tr_tr", "uk_ua", "zh_cn");
    /** The horse has 30 health: two rows of mount hearts. */
    private static final String HORSE = "minecraft:horse 0.5 -60 2.5 {Tame:1b,Health:23f,"
            + "attributes:[{id:\"minecraft:max_health\",base:30d}],"
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
            check(context, "the mount's hearts go left, above armor", () -> left(context, server, "en_"));
            check(context, "the jump bar shows while charging a jump", () -> jumpBar(context, "en_"));
            check(context, "the settings screen resets the settings", () -> settingsScreen(context, "en"));

            // Russian strings are longer than English ones
            setLanguage(context, "ru_ru");
            check(context, "Russian: HUD and settings", () -> {
                aboveHunger(context, "ru_");
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
        context.waitTicks(3);
        apply(context, s -> s.heartsPosition = HeartsPosition.LEFT);
        BufferedImage modded = shot(context, prefix + "left");
        Region row = context.computeOnClient(mc -> Region.leftRow(mc,
                HudLayout.leftFreeRow(height(mc), mc.player.getMaxHealth(), mc.player.getAbsorptionAmount(),
                        mc.player.getArmorValue())));
        apply(context, s -> s.enabled = false);
        BufferedImage vanilla = shot(context, prefix + "left_vanilla");
        server.runCommand("item replace entity @a armor.chest with minecraft:air");
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

    private void settingsScreen(ClientGameTestContext context, String lang) {
        apply(context, s -> {
            s.heartsPosition = HeartsPosition.LEFT;
            s.jumpBar = JumpBarMode.ALWAYS_JUMP;
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
        context.getInput().resizeWindow(3840, 2160);
        context.runOnClient(mc -> TestCompat.guiScale(mc, STORE_GUI_SCALE));
        context.waitTicks(5);
        apply(context, s -> s.heartsPosition = HeartsPosition.ABOVE_HUNGER);
        shot(context, "store_above_hunger");
        context.getInput().holdKey(options -> options.keyJump);
        context.waitTicks(8);
        shot(context, "store_jump_bar");
        context.getInput().releaseKey(options -> options.keyJump);
        context.waitTicks(30);
        server.runCommand("item replace entity @a armor.chest with minecraft:iron_chestplate");
        apply(context, s -> s.heartsPosition = HeartsPosition.LEFT);
        shot(context, "store_left");
        server.runCommand("item replace entity @a armor.chest with minecraft:air");
        apply(context, s -> s.enabled = false);
        shot(context, "store_vanilla");
        apply(context, s -> { });
        context.runOnClient(mc -> TestCompat.guiScale(mc, 4));
        context.setScreen(() -> new SettingsScreen(null));
        context.takeScreenshot("store_settings");
        context.setScreen(() -> null);
        context.runOnClient(mc -> TestCompat.guiScale(mc, 0));
        context.getInput().resizeWindow(854, 480);
        context.waitTicks(2);
    }

    // --- Helpers --------------------------------------------------------------------------------

    /** A tame, saddled horse with the player on it, the player hungry and with some experience. */
    private static void mount(ClientGameTestContext context, TestServerContext server) {
        server.runCommand("summon " + HORSE);
        server.runCommand("ride @p mount @e[type=minecraft:horse,limit=1]");
        server.runCommand("xp set @a 7 levels");
        server.runCommand("xp add @a 20 points");
        server.runCommand("effect give @a minecraft:hunger 2 200 true");
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

    /** One row of ten hearts on the HUD, in screenshot pixels. */
    private record Region(int x, int y, int width, int height, int scale) {
        static Region rightRow(net.minecraft.client.Minecraft mc, int row) {
            int center = mc.getWindow().getGuiScaledWidth() / 2;
            return of(mc, center + 11, row);
        }

        static Region leftRow(net.minecraft.client.Minecraft mc, int row) {
            int center = mc.getWindow().getGuiScaledWidth() / 2;
            return of(mc, center - 91, row);
        }

        private static Region of(net.minecraft.client.Minecraft mc, int x, int row) {
            int scale = (int) Math.round(mc.getWindow().getGuiScale());
            return new Region(x * scale, row * scale, 80 * scale, 9 * scale, scale);
        }

        /** Fewer heart pixels than a few hearts have: no hearts in the row. */
        int minHearts() {
            return 40 * scale * scale;
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
    }
}
