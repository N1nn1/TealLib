package com.ninni.teallib.api.common.data.variantdata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ninni.teallib.core.registry.VariantDataTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

/**
 * Makes a mob persistent, preventing it from naturally despawning.
 *
 * @param chance the chance, from {@code 0.0} to {@code 1.0}, that this Variant Data is applied.
 */
public record PersistentData(float chance) implements VariantData {
    public static final MapCodec<PersistentData> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.FLOAT.optionalFieldOf("chance", 1f).forGetter(PersistentData::chance)
    ).apply(inst, PersistentData::new));

    @Override
    public void applyEntity(Entity entity, ServerLevel level, RandomSource random) {
        if (random.nextFloat() > chance) return;
        if (entity instanceof Mob mob) mob.setPersistenceRequired();
    }

    @Override
    public MapCodec<? extends VariantData> type() {
        return VariantDataTypes.PERSISTENT.get();
    }
}