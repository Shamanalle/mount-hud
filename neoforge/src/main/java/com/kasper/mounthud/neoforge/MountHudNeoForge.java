package com.kasper.mounthud.neoforge;

import com.kasper.mounthud.MountHud;
import com.kasper.mounthud.client.SettingsScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** NeoForge entrypoint. The mod only changes the HUD: on a dedicated server it loads and does nothing. */
@Mod(MountHud.MOD_ID)
public final class MountHudNeoForge {
    private static final Logger LOG = LogManager.getLogger(MountHud.NAME);

    public MountHudNeoForge(IEventBus modBus, ModContainer container, Dist dist) {
        if (dist == Dist.CLIENT) {
            Client.init(container);
        }
    }

    /** Client classes stay out of the constructor, so a server never loads them. */
    private static final class Client {
        static void init(ModContainer container) {
            MountHud.init(FMLPaths.CONFIGDIR.get(), LOG::warn);
            LOG.info("{} initialized", MountHud.NAME);
            NeoForge.EVENT_BUS.addListener(HudHook::afterVehicleHealth);
            container.registerExtensionPoint(IConfigScreenFactory.class, (mod, parent) -> new SettingsScreen(parent));
        }
    }
}
