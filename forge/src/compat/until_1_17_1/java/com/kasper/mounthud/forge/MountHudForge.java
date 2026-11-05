package com.kasper.mounthud.forge;

import com.kasper.mounthud.MountHud;
import com.kasper.mounthud.client.SettingsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fmlclient.ConfigGuiHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Forge entrypoint (Minecraft 1.17.1: ConfigGuiHandler in net.minecraftforge.fmlclient). */
@Mod(MountHud.MOD_ID)
public final class MountHudForge {
    private static final Logger LOG = LogManager.getLogger(MountHud.NAME);

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
            ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class,
                    () -> new ConfigGuiHandler.ConfigGuiFactory((minecraft, parent) -> new SettingsScreen(parent)));
        }
    }
}
