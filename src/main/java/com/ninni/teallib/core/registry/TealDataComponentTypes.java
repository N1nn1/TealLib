package com.ninni.teallib.core.registry;

import com.ninni.teallib.core.TealLib;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.List;

public final class TealDataComponentTypes {
    public static final DeferredRegister<DataComponentType<?>> DEF_REG = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, TealLib.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ItemStack>>> STACK_VARIANTS = DEF_REG.register("stack_variants", () -> DataComponentType.<List<ItemStack>>builder().persistent(CompressedItemStackListCodec.create()).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> STACK_VARIANT_INDEX = DEF_REG.register("stack_variant_index", () -> DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build());
}
