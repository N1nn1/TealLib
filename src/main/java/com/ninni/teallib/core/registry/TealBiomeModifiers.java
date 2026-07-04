package com.ninni.teallib.core.registry;

import com.mojang.serialization.MapCodec;
import com.ninni.teallib.core.TealLib;
import com.ninni.teallib.api.common.level.worldgen.modifier.FilteredSpawnsModifier;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class TealBiomeModifiers {
    public static final DeferredRegister<MapCodec<? extends BiomeModifier>> DEF_REG = DeferredRegister.create(NeoForgeRegistries.BIOME_MODIFIER_SERIALIZERS, TealLib.MODID);


    public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<FilteredSpawnsModifier>> ADD_FILTERED_SPAWNS = DEF_REG.register("add_filtered_spawns", () -> FilteredSpawnsModifier.CODEC);

}
