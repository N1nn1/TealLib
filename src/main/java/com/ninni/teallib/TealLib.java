package com.ninni.teallib;

import com.ninni.teallib.registry.TealEntityType;
import com.ninni.teallib.registry.TealItems;
import com.ninni.teallib.registry.VariantDataTypes;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(TealLib.MODID)
public class TealLib {
    public static final String MODID = "teallib";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TealLib(IEventBus modEventBus, ModContainer modContainer) {
        VariantDataTypes.TYPES.register(modEventBus);
        TealItems.ITEMS.register(modEventBus);
        TealEntityType.ENTITY_TYPES.register(modEventBus);
    }
}
