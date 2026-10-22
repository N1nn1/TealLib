package com.ninni.teallib.api.client.particle.options;

import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.NotNull;

public class EntityParticleOptions implements ParticleOptions {
    private final ParticleType<EntityParticleOptions> type;
    private final int entityId;

    public static MapCodec<EntityParticleOptions> codec(ParticleType<EntityParticleOptions> particleType) {
        return ExtraCodecs.ARGB_COLOR_CODEC.xmap((p_333828_) -> new EntityParticleOptions(particleType, p_333828_), (p_333908_) -> p_333908_.entityId).fieldOf("entity_id");
    }

    public static StreamCodec<? super ByteBuf, EntityParticleOptions> streamCodec(ParticleType<EntityParticleOptions> type) {
        return ByteBufCodecs.INT.map((p_333912_) -> new EntityParticleOptions(type, p_333912_), (p_334072_) -> p_334072_.entityId);
    }

    public EntityParticleOptions(ParticleType<EntityParticleOptions> type, int entityId) {
        this.type = type;
        this.entityId = entityId;
    }

    public int getEntityId() {
        return entityId;
    }

    @Override
    public @NotNull ParticleType<?> getType() {
        return type;
    }
}
