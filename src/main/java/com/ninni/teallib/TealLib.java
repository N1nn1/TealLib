package com.ninni.teallib;

import com.ninni.teallib.client.TealLibClientConfig;
import com.ninni.teallib.registry.TealBiomeModifiers;
import com.ninni.teallib.registry.TealEntityType;
import com.ninni.teallib.registry.TealItems;
import com.ninni.teallib.registry.VariantDataTypes;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(TealLib.MODID)
public class TealLib {
    public static final String MODID = "teallib";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final TealLibClientConfig CLIENT_CONFIG;
    private static final ModConfigSpec CLIENT_CONFIG_SPEC;

    static {
        final Pair<TealLibClientConfig, ModConfigSpec> clientPair = new ModConfigSpec.Builder().configure(TealLibClientConfig::new);
        CLIENT_CONFIG = clientPair.getLeft();
        CLIENT_CONFIG_SPEC = clientPair.getRight();
    }

    public TealLib(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, CLIENT_CONFIG_SPEC, "teallib-client.toml");
        VariantDataTypes.DEF_REG.register(modEventBus);
        TealItems.DEF_REG.register(modEventBus);
        TealEntityType.DEF_REG.register(modEventBus);
        TealBiomeModifiers.DEF_REG.register(modEventBus);
    }
}
