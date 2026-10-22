package com.ninni.teallib.api.client.particle.options;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public class ArrivingParticleOption implements ParticleOptions {
    private final ParticleType<ArrivingParticleOption> type;
    private final int arrivalInTicks;

    public static MapCodec<ArrivingParticleOption> codec(ParticleType<ArrivingParticleOption> particleType) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(Codec.INT.fieldOf("arrival_in_ticks").forGetter(ArrivingParticleOption::getArrivalInTicks)).apply(instance, (arrival) -> new ArrivingParticleOption(particleType, arrival))
        );
    }

    public static StreamCodec<RegistryFriendlyByteBuf, ArrivingParticleOption> streamCodec(ParticleType<ArrivingParticleOption> type) {
        return StreamCodec.composite(
                ByteBufCodecs.INT,
                ArrivingParticleOption::getArrivalInTicks,
                (arrival) -> new ArrivingParticleOption(type, arrival)
        );
    }

    public ArrivingParticleOption(ParticleType<ArrivingParticleOption> type, int arrivalInTicks) {
        this.type = type;
        this.arrivalInTicks = arrivalInTicks;
    }

    public int getArrivalInTicks() {
        return arrivalInTicks;
    }

    @Override
    public @NotNull ParticleType<?> getType() {
        return type;
    }
}
