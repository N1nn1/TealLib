package com.ninni.teallib.server.data.variantdata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.ninni.teallib.registry.VariantDataTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;
import java.util.function.Function;

public interface VariantData {
    default void applyEntity(Entity entity, ServerLevel level, RandomSource random) {
    }
    default void applyBlockEntity(BlockEntity entity, ServerLevel level, RandomSource random) {
    }
    MapCodec<? extends VariantData> type();
    Codec<VariantData> CODEC = VariantDataTypes.REGISTRY.byNameCodec().dispatch(VariantData::type, Function.identity());
    Codec<List<VariantData>> LIST_CODEC = CODEC.listOf();
}