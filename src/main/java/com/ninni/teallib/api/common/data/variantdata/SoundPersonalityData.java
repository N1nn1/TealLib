package com.ninni.teallib.api.common.data.variantdata;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ninni.teallib.api.common.entity.sound.SoundPersonality;
import com.ninni.teallib.api.common.entity.sound.SoundPersonalityHolder;
import com.ninni.teallib.core.registry.VariantDataTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ServerLevelAccessor;

public record SoundPersonalityData(SoundPersonality soundPersonality) implements VariantData {
    public static final MapCodec<SoundPersonalityData> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            SoundPersonality.CODEC.fieldOf("personality").forGetter(SoundPersonalityData::soundPersonality)
    ).apply(inst, SoundPersonalityData::new));

    @Override
    public void applyEntity(Entity entity, ServerLevelAccessor level, RandomSource random) {
        if (entity instanceof SoundPersonalityHolder mob) mob.setPersonality(soundPersonality);
    }

    @Override
    public MapCodec<? extends VariantData> type() {
        return VariantDataTypes.SOUND_PERSONALITY.get();
    }
}