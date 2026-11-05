package com.kasper.mounthud.neoforge;

import com.kasper.mounthud.MountHud;
import com.kasper.mounthud.client.SettingsScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** NeoForge entrypoint. The mod only changes the HUD: on a dedicated server it loads and does nothing. */
@Mod(MountHud.MOD_ID)
public final class MountHudNeoForge {
    private static final Logger LOG = LogManager.getLogger(MountHud.NAME);

    public MountHudNeoForge(IEventBus modBus, ModContainer container, Dist dist) {
        if (dist == Dist.CLIENT) {
            Client.init(modBus, container);
        }
    }

    /** Client classes stay out of the constructor, so a server never loads them. */
    private static final class Client {
        static void init(IEventBus modBus, ModContainer container) {
            MountHud.init(FMLPaths.CONFIGDIR.get(), LOG::warn);
            LOG.info("{} initialized", MountHud.NAME);
            modBus.addListener(HudHook::registerLayers);
            container.registerExtensionPoint(IConfigScreenFactory.class, (mod, parent) -> new SettingsScreen(parent));
        }
    }
}
