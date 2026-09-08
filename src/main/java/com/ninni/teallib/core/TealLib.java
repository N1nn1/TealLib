package com.ninni.teallib.core;

import com.ninni.teallib.api.common.data.variant.VariantManager;
import com.ninni.teallib.core.registry.VanillaEntityVariantComponents;
import com.ninni.teallib.core.client.TealLibClientConfig;
import com.ninni.teallib.core.registry.*;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod(TealLib.MODID)
public class TealLib {
    public static final String MODID = "teallib";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final TealLibClientConfig CLIENT_CONFIG;
    public static final TealLibCommonConfig COMMON_CONFIG;
    private static final ModConfigSpec CLIENT_CONFIG_SPEC;
    private static final ModConfigSpec COMMON_CONFIG_SPEC;

    static {
        final Pair<TealLibClientConfig, ModConfigSpec> clientPair = new ModConfigSpec.Builder().configure(TealLibClientConfig::new);
        CLIENT_CONFIG = clientPair.getLeft();
        CLIENT_CONFIG_SPEC = clientPair.getRight();
        final Pair<TealLibCommonConfig, ModConfigSpec> commonPair = new ModConfigSpec.Builder().configure(TealLibCommonConfig::new);
        COMMON_CONFIG = commonPair.getLeft();
        COMMON_CONFIG_SPEC = commonPair.getRight();
    }

    public TealLib(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, CLIENT_CONFIG_SPEC, "teallib/client.toml");
        modContainer.registerConfig(ModConfig.Type.COMMON, COMMON_CONFIG_SPEC, "teallib/common.toml");
        VariantDataTypes.DEF_REG.register(modEventBus);
        TealItems.DEF_REG.register(modEventBus);
        TealEntityType.DEF_REG.register(modEventBus);
        TealAttachments.DEF_REG.register(modEventBus);
        TealBiomeModifiers.DEF_REG.register(modEventBus);
        TealParticleType.DEF_REG.register(modEventBus);
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> VariantManager.tickAwaitingVariants());
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> VariantManager.clearAwaitingVariants());

        VanillaEntityVariantComponents.register();
        if (FMLEnvironment.dist == Dist.CLIENT) VanillaVariantTextureSlots.init();
    }
}
