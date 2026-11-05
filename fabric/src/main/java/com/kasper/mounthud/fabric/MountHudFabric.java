package com.kasper.mounthud.fabric;

import com.kasper.mounthud.MountHud;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Fabric and Quilt entrypoint (client only). */
public final class MountHudFabric implements ClientModInitializer {
    private static final Logger LOG = LoggerFactory.getLogger(MountHud.NAME);

    @Override
    public void onInitializeClient() {
        MountHud.init(FabricLoader.getInstance().getConfigDir(), LOG::warn);
        LOG.info("{} initialized", MountHud.NAME);
    }
}
