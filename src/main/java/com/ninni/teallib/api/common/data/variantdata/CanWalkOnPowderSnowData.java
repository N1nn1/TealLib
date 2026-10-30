package com.ninni.teallib.api.common.data.variantdata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ninni.teallib.core.registry.TealAttachments;
import com.ninni.teallib.core.registry.VariantDataTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * Makes a mob able to walk on Powder Snow.
 *
 * @param chance the chance, from {@code 0.0} to {@code 1.0}, that this Variant Data is applied.
 */
public record CanWalkOnPowderSnowData(float chance) implements VariantData {
    public static final MapCodec<CanWalkOnPowderSnowData> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.FLOAT.optionalFieldOf("chance", 1f).forGetter(CanWalkOnPowderSnowData::chance)
    ).apply(inst, CanWalkOnPowderSnowData::new));

    @Override
    public void applyEntity(Entity entity, ServerLevelAccessor level, RandomSource random) {
        if (random.nextFloat() > chance) return;
        entity.setData(TealAttachments.CAN_WALK_ON_POWDER_SNOW.get(), true);
    }

    @Override
    public MapCodec<? extends VariantData> type() {
        return VariantDataTypes.CAN_WALK_ON_POWDER_SNOW.get();
    }
}