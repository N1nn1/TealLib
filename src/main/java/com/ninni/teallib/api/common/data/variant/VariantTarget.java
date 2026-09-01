package com.ninni.teallib.api.common.data.variant;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Identifies something that a VariantData can be attached to
 * <p>
 * A variant target can be an Entity, a Block Entity, a Particle, or custom one.
 * Custom targets are useful for particles, render-only
 * objects, projectiles, animations, and other systems that do not have a
 * Minecraft registry type.
 */
public record VariantTarget(Kind kind, ResourceLocation id) {
    public static final Codec<Kind> KIND_CODEC =
            Codec.STRING.comapFlatMap(
                    value -> switch (value) {
                        case "entity_type" -> DataResult.success(Kind.ENTITY_TYPE);
                        case "block_entity_type" -> DataResult.success(Kind.BLOCK_ENTITY_TYPE);
                        case "particle_type" -> DataResult.success(Kind.PARTICLE_TYPE);
                        case "custom" -> DataResult.success(Kind.CUSTOM);
                        default -> DataResult.error(() -> "Unknown variant target kind: " + value);
                    },
                    Kind::id
            );

    public static final Codec<VariantTarget> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            KIND_CODEC.fieldOf("kind").forGetter(VariantTarget::kind),
                            ResourceLocation.CODEC.fieldOf("id").forGetter(VariantTarget::id)
                    ).apply(instance, VariantTarget::new)
            );


    public static VariantTarget of(EntityType<?> type) {
        return new VariantTarget(Kind.ENTITY_TYPE, BuiltInRegistries.ENTITY_TYPE.getKey(type));
    }
    public static VariantTarget of(BlockEntityType<?> type) {
        return new VariantTarget(Kind.BLOCK_ENTITY_TYPE, BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type));
    }
    public static VariantTarget of(ParticleType<?> type) {
        return new VariantTarget(Kind.PARTICLE_TYPE, BuiltInRegistries.PARTICLE_TYPE.getKey(type));
    }
    public static VariantTarget custom(ResourceLocation id) {
        return new VariantTarget(Kind.CUSTOM, id);
    }

    public boolean isEntityType() {
        return kind == Kind.ENTITY_TYPE;
    }
    public boolean isBlockEntityType() {
        return kind == Kind.BLOCK_ENTITY_TYPE;
    }
    public boolean isParticleType() {
        return kind == Kind.PARTICLE_TYPE;
    }
    public boolean isCustom() {
        return kind == Kind.CUSTOM;
    }

    public enum Kind {
        ENTITY_TYPE("entity_type"),
        BLOCK_ENTITY_TYPE("block_entity_type"),
        PARTICLE_TYPE("particle_type"),
        CUSTOM("custom");

        private final String id;

        Kind(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }
    }
}