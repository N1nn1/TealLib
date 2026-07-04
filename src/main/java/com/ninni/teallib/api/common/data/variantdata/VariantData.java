package com.ninni.teallib.api.common.data.variantdata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.ninni.teallib.core.registry.VariantDataTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;
import java.util.function.Function;

/**
 * Represents additional data that can be applied when a variant is assigned.
 * <p>
 * Variant Data allows variants to modify entities or block entities by applying
 * additional behavior or properties, such as attributes, age, persistence or
 * custom data.
 * <p>
 * Each implementation is serialized using its registered codec and is applied
 * automatically by the variant managers when the variant is assigned.
 */
public interface VariantData {

    /**
     * Applies this Variant Data to an Entity.
     *
     * @param entity The Entity receiving the Variant Data.
     * @param level The Server Level.
     * @param random The Random Source used for chance-based effects.
     */
    default void applyEntity(Entity entity, ServerLevel level, RandomSource random) {
    }

    /**
     * Applies this Variant Data to a Block Entity.
     *
     * @param entity The Block Entity receiving the Variant Data.
     * @param level The Server Level.
     * @param random The Random Source used for chance-based effects.
     */
    default void applyBlockEntity(BlockEntity entity, ServerLevel level, RandomSource random) {
    }

    MapCodec<? extends VariantData> type();
    Codec<VariantData> CODEC = VariantDataTypes.REGISTRY.byNameCodec().dispatch(VariantData::type, Function.identity());
    Codec<List<VariantData>> LIST_CODEC = CODEC.listOf();
}