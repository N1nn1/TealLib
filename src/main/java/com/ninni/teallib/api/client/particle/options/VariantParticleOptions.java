package com.ninni.teallib.api.client.particle.options;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ninni.teallib.api.common.data.variant.VariantDefinition;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public class VariantParticleOptions implements ParticleOptions {
    private final ParticleType<VariantParticleOptions> type;
    private final VariantDefinition variant;

    public static MapCodec<VariantParticleOptions> codec(ParticleType<VariantParticleOptions> particleType) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(VariantDefinition.CODEC.fieldOf("variant").forGetter(VariantParticleOptions::getVariant)).apply(instance, (variant) -> new VariantParticleOptions(particleType, variant))
        );
    }

    public static StreamCodec<RegistryFriendlyByteBuf, VariantParticleOptions> streamCodec(ParticleType<VariantParticleOptions> type) {
        return StreamCodec.composite(
                VariantDefinition.STREAM_CODEC,
                VariantParticleOptions::getVariant,
                (variantDefinition) -> new VariantParticleOptions(type, variantDefinition)
        );
    }

    public VariantParticleOptions(ParticleType<VariantParticleOptions> type, VariantDefinition variant) {
        this.type = type;
        this.variant = variant;
    }

    public VariantDefinition getVariant() {
        return variant;
    }

    @Override
    public @NotNull ParticleType<?> getType() {
        return type;
    }
}
