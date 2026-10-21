package com.kasper.mounthud.forge;

import com.kasper.mounthud.MountHud;
import com.kasper.mounthud.client.SettingsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Forge entrypoint. The mod only changes the HUD: on a dedicated server it loads and does nothing. */
@Mod(MountHud.MOD_ID)
public final class MountHudForge {
    private static final Logger LOG = LoggerFactory.getLogger(MountHud.NAME);

    public MountHudForge() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            Client.init();
        }
    }

    /** Client classes stay out of the constructor, so a server never loads them. */
    private static final class Client {
        static void init() {
            MountHud.init(FMLPaths.CONFIGDIR.get(), LOG::warn);
            LOG.info("{} initialized", MountHud.NAME);
            MinecraftForge.EVENT_BUS.register(new HudHook());
            ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                    () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> new SettingsScreen(parent)));
        }
    }
}
