package com.ninni.teallib.common.data.variantdata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ninni.teallib.registry.VariantDataTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;

public record AgeData(float chance, int age) implements VariantData {
    public static final MapCodec<AgeData> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.FLOAT.optionalFieldOf("chance", 1f).forGetter(AgeData::chance),
            Codec.INT.optionalFieldOf("age", 0).forGetter(AgeData::age)
    ).apply(inst, AgeData::new));

    @Override
    public void applyEntity(Entity entity, ServerLevel level, RandomSource random) {
        if (random.nextFloat() > chance) return;
        if (entity instanceof AgeableMob mob) mob.setAge(age);
    }

    @Override
    public MapCodec<? extends VariantData> type() {
        return VariantDataTypes.AGE.get();
    }
}