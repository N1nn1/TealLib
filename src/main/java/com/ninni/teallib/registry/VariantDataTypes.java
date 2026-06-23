package com.ninni.teallib.registry;

import com.mojang.serialization.MapCodec;
import com.ninni.teallib.TealLib;
import com.ninni.teallib.server.data.variantdata.AgeData;
import com.ninni.teallib.server.data.variantdata.AttributeData;
import com.ninni.teallib.server.data.variantdata.PersistentData;
import com.ninni.teallib.server.data.variantdata.VariantData;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class VariantDataTypes {
    public static final ResourceKey<Registry<MapCodec<? extends VariantData>>> KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "variant_data_type"));
    public static final DeferredRegister<MapCodec<? extends VariantData>> TYPES = DeferredRegister.create(KEY, TealLib.MODID);
    public static final Registry<MapCodec<? extends VariantData>> REGISTRY = TYPES.makeRegistry(builder -> {});

    public static final DeferredHolder<MapCodec<? extends VariantData>, MapCodec<PersistentData>> PERSISTENT = TYPES.register("persistent", () -> PersistentData.CODEC);
    public static final DeferredHolder<MapCodec<? extends VariantData>, MapCodec<AgeData>> AGE = TYPES.register("age", () -> AgeData.CODEC);
    public static final DeferredHolder<MapCodec<? extends VariantData>, MapCodec<AttributeData>> ATTRIBUTE = TYPES.register("attribute", () -> AttributeData.CODEC);
}
