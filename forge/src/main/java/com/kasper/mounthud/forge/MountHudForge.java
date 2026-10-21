package com.kasper.mounthud.forge;

import com.kasper.mounthud.MountHud;
import net.minecraftforge.api.distmarker.Dist;
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
            MountHud.init(FMLPaths.CONFIGDIR.get(), LOG::warn);
        }
    }
}
