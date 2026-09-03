package com.ninni.teallib.api.common.data.variant;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ninni.teallib.api.common.data.CodecUtils;
import com.ninni.teallib.api.common.data.variantdata.VariantData;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Defines a json variant.
 */
public record VariantDefinition(
        List<VariantTarget> targets,
        ResourceLocation id,

        Optional<HolderSet<Biome>> location,
        CodecUtils.Weather weather,

        Optional<String> comment,

        Optional<Integer> spawnWeight,
        Optional<Integer> maxSpawnHeight,
        Optional<Integer> minSpawnHeight,

        Optional<CodecUtils.NameTagRule> nameTag,

        boolean hidden,

        Optional<List<VariantData>> variantData,
        Map<String, ResourceLocation> textures,
        Map<ResourceLocation, ResourceLocation> textureOverrides,
        Map<ResourceLocation, ResourceLocation> babyTextureOverrides,
        int priority,
        boolean keepVanillaTexture
) {

    public static final Codec<VariantDefinition> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            VariantTarget.CODEC.listOf().fieldOf("targets").forGetter(VariantDefinition::targets),
                            ResourceLocation.CODEC.fieldOf("id").forGetter(VariantDefinition::id),
                            Biome.LIST_CODEC.optionalFieldOf("location").forGetter(VariantDefinition::location),
                            CodecUtils.Weather.CODEC.fieldOf("weather").orElse(CodecUtils.Weather.NONE).forGetter(VariantDefinition::weather),
                            Codec.STRING.optionalFieldOf("comment").forGetter(VariantDefinition::comment),
                            Codec.INT.optionalFieldOf("spawnWeight").forGetter(VariantDefinition::spawnWeight),
                            Codec.INT.optionalFieldOf("maxSpawnHeight").forGetter(VariantDefinition::maxSpawnHeight),
                            Codec.INT.optionalFieldOf("minSpawnHeight").forGetter(VariantDefinition::minSpawnHeight),
                            CodecUtils.NameTagRule.CODEC.optionalFieldOf("nameTag").forGetter(VariantDefinition::nameTag),
                            Codec.BOOL.fieldOf("hidden").orElse(false).forGetter(VariantDefinition::hidden),
                            VariantData.LIST_CODEC.optionalFieldOf("variant_data_types").forGetter(VariantDefinition::variantData),
                            Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC).optionalFieldOf("textures", Map.of()).forGetter(VariantDefinition::textures),
                            Codec.unboundedMap(ResourceLocation.CODEC, ResourceLocation.CODEC).optionalFieldOf("texture_overrides", Map.of()).forGetter(VariantDefinition::textureOverrides),
                            Codec.unboundedMap(ResourceLocation.CODEC, ResourceLocation.CODEC).optionalFieldOf("baby_texture_overrides", Map.of()).forGetter(VariantDefinition::babyTextureOverrides),
                            Codec.INT.optionalFieldOf("priority", 0).forGetter(VariantDefinition::priority),
                            Codec.BOOL.optionalFieldOf("keep_vanilla_texture", false).forGetter(VariantDefinition::keepVanillaTexture)

                    ).apply(instance, VariantDefinition::new)
            );

    public boolean supports(VariantTarget target) {
        return targets.contains(target);
    }

    /** Biome specific entries outrank generic ones unless a pack says otherwise. */
    public int effectivePriority() {
        return priority + (location.isPresent() ? 1 : 0);
    }
    public boolean hasTexture(String target) {
        return textures.containsKey(target);
    }
    public Optional<ResourceLocation> texture(String target) {
        return Optional.ofNullable(textures.get(target));
    }

    /** Replacement keyed on the texture about to be drawn, so overlays need no Java slot. */
    public Optional<ResourceLocation> overrideFor(ResourceLocation original, boolean baby) {
        if (original == null) return Optional.empty();
        if (baby) {
            ResourceLocation babyOverride = babyTextureOverrides.get(original);
            if (babyOverride != null) return Optional.of(babyOverride);
        }
        return Optional.ofNullable(textureOverrides.get(original));
    }
}