package com.kasper.mounthud.neoforge;

import com.kasper.mounthud.MountHud;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** NeoForge entrypoint (client only: a dedicated server skips the mod). */
@Mod(value = MountHud.MOD_ID, dist = Dist.CLIENT)
public final class MountHudNeoForge {
    private static final Logger LOG = LoggerFactory.getLogger(MountHud.NAME);

    public MountHudNeoForge(IEventBus modBus, ModContainer container) {
        MountHud.init(FMLPaths.CONFIGDIR.get(), LOG::warn);
    }
}
