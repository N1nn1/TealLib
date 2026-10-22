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

public class ArrivingEntityParticleOption implements ParticleOptions {
    private final ParticleType<ArrivingEntityParticleOption> type;
    private final int arrivalInTicks;
    private final int entityId;

    public static MapCodec<ArrivingEntityParticleOption> codec(ParticleType<ArrivingEntityParticleOption> particleType) {
        return RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Codec.INT.fieldOf("arrival_in_ticks").forGetter(ArrivingEntityParticleOption::getArrivalInTicks),
                        Codec.INT.fieldOf("entity_id").forGetter(ArrivingEntityParticleOption::getEntityId)
                        )
                .apply(instance, (arrival, id) -> new ArrivingEntityParticleOption(particleType, arrival, id))
        );
    }

    public static StreamCodec<RegistryFriendlyByteBuf, ArrivingEntityParticleOption> streamCodec(ParticleType<ArrivingEntityParticleOption> type) {
        return StreamCodec.composite(
                ByteBufCodecs.INT,
                ArrivingEntityParticleOption::getArrivalInTicks,
                ByteBufCodecs.INT,
                ArrivingEntityParticleOption::getEntityId,
                (arrival, id) -> new ArrivingEntityParticleOption(type, arrival, id)
        );
    }

    public ArrivingEntityParticleOption(ParticleType<ArrivingEntityParticleOption> type, int arrivalInTicks, int entityId) {
        this.type = type;
        this.arrivalInTicks = arrivalInTicks;
        this.entityId = entityId;
    }

    public int getArrivalInTicks() {
        return arrivalInTicks;
    }
    
    public int getEntityId() {
        return entityId;
    }

    @Override
    public @NotNull ParticleType<?> getType() {
        return type;
    }
}
