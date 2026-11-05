package com.kasper.mounthud.boot;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Forge entrypoint: starts the variant for the running Minecraft version. Forge 37 - 47 call a constructor
 * without parameters, newer Forge prefers the one with the loading context.
 */
@Mod("mounthud")
public final class ForgeBoot {
    public ForgeBoot() {
        this((FMLJavaModLoadingContext) Variants.call("net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext", "get"));
    }

    public ForgeBoot(FMLJavaModLoadingContext context) {
        if (Variants.client()) {
            Variants.create("entry", context == null ? new Object[0] : new Object[] {context});
        }
    }
}
