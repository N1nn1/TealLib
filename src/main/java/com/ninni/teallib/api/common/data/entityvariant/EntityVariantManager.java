package com.ninni.teallib.api.common.data.entityvariant;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ninni.teallib.api.common.data.variantdata.VariantData;
import com.ninni.teallib.api.common.entity.variant.JsonVariantHolder;
import com.ninni.teallib.api.common.data.CodecUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A utility class for working with {@link EntityVariantData Entity Variant Data}.
 * <p>
 * It provides methods for querying registered variants, selecting naturally
 * occurring variants based on biome and world conditions, applying
 * {@link com.ninni.teallib.api.common.data.variantdata.VariantData Variant Data},
 * and resolving textures for entities implementing
 * {@link com.ninni.teallib.api.common.entity.variant.JsonVariantHolder JsonVariantHolder}.
 * <p>
 * Variant selection supports biome restrictions, weather, spawn height,
 * weighted random spawning and name tag overrides.
 */
public class EntityVariantManager {

    private EntityVariantManager() {}

    /**
     * @param access The Registry Access.
     * @return the Entity Variant registry.
     */
    public static Registry<EntityVariantData> registry(RegistryAccess access) {
        return access.registryOrThrow(EntityVariantRegistries.ENTITY_VARIANT_REGISTRY_KEY);
    }

    /**
     * @param access The Registry Access.
     * @return every registered Entity Variant.
     */
    public static List<EntityVariantData> all(RegistryAccess access) {
        List<EntityVariantData> out = new ArrayList<>();
        for (Map.Entry<ResourceKey<EntityVariantData>, EntityVariantData> entry : registry(access).entrySet()) {
            out.add(entry.getValue());
        }
        return out;
    }

    /**
     * Retrieves an Entity Variant by its ID.
     *
     * @param access The Registry Access.
     * @param id The Variant ID.
     * @return the matching Entity Variant, or {@code null} if none exists.
     */
    @Nullable
    public static EntityVariantData get(RegistryAccess access, ResourceLocation id) {
        for (EntityVariantData data : all(access)) {
            if (data.id().equals(id)) return data;
        }
        return null;
    }

    /**
     * Retrieves an Entity Variant matching both an entity type and variant ID.
     *
     * @param access The Registry Access.
     * @param type The Entity Type.
     * @param id The Variant ID.
     * @return the matching Entity Variant, or {@code null} if none exists.
     */
    @Nullable
    public static EntityVariantData getForEntityType(RegistryAccess access, EntityType<?> type, ResourceLocation id) {
        for (EntityVariantData data : all(access)) {
            if (data.id().equals(id) && matchesEntityType(type, data)) return data;
        }
        return null;
    }

    /**
     * Counts the number of variants registered for an Entity Type.
     *
     * @param access The Registry Access.
     * @param type The Entity Type.
     * @return the amount of registered variants.
     */
    public static int getVariantCountFor(RegistryAccess access, EntityType<?> type) {
        int count = 0;
        for (EntityVariantData data : all(access)) {
            if (matchesEntityType(type, data)) count++;
        }
        return count;
    }

    /**
     * @param access The Registry Access.
     * @param id The Variant ID.
     * @return whether the Variant ID exists.
     */
    public static boolean isValidVariant(RegistryAccess access, ResourceLocation id) {
        return get(access, id) != null;
    }

    /**
     * @param access The Registry Access.
     * @param type The Entity Type.
     * @param id The Variant ID.
     * @return whether the Variant ID exists for the specified Entity Type.
     */
    public static boolean isValidVariantForType(RegistryAccess access, EntityType<?> type, ResourceLocation id) {
        return getForEntityType(access, type, id) != null;
    }

    /**
     * Chooses and applies a naturally occurring variant to an entity.
     * <p>
     * The selected variant depends on the current biome, weather,
     * spawn height and configured spawn weights. If no matching
     * variant can be found, the entity's default variant is used.
     *
     * @param entity The Entity to assign a variant to.
     */
    public static void getNaturallyOccurringVariant(Entity entity) {
        if (!(entity instanceof JsonVariantHolder variantHolder)) return;

        Optional<WeightedEntry> variant = chooseVariant(entity.getType(), entity.level(), entity.blockPosition());
        ResourceLocation id = variant.map(WeightedEntry::id).orElseGet(variantHolder::getDefaultVariant);

        EntityVariantData data = getForEntityType(entity.level().registryAccess(), entity.getType(), id);

        if (data != null && matchesEntityType(entity.getType(), data)) {
            variantHolder.setVariant(data.id());
            applyVariantData(entity, data);
        }
    }

    /**
     * Applies every configured {@link VariantData Variant Data}
     * contained within an Entity Variant.
     *
     * @param mob The Entity to apply Variant Data to.
     * @param data The Entity Variant containing the Variant Data.
     */
    public static void applyVariantData(Entity mob, EntityVariantData data) {
        if (data == null || data.variantData.isEmpty()) return;

        Level level = mob.level();
        if (!(level instanceof ServerLevel serverLevel)) return;

        RandomSource random = mob.getRandom();
        for (VariantData variantData : data.variantData.get()) {
            variantData.applyEntity(mob, serverLevel, random);
        }
    }

    /**
     * Chooses a weighted variant for an Entity Type at a given position.
     * <p>
     * Variants are filtered using biome, weather and spawn height
     * restrictions before being selected using their configured
     * spawn weights.
     *
     * @param type The Entity Type.
     * @param level The Level.
     * @param pos The position the Entity is spawning at.
     * @return the chosen weighted entry, or an empty Optional if
     * no valid variants were found.
     */
    public static Optional<WeightedEntry> chooseVariant(EntityType<?> type, Level level, BlockPos pos) {
        Holder<Biome> holder = level.getBiome(pos);
        CodecUtils.Weather weather = level.isRaining() && level.canSeeSky(pos) && level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos).getY() <= pos.getY() && holder.value().getPrecipitationAt(pos) == Biome.Precipitation.SNOW
                ? CodecUtils.Weather.SNOW
                : level.isRainingAt(pos) && level.isThundering()
                  ? CodecUtils.Weather.THUNDER
                  : level.isRainingAt(pos)
                    ? CodecUtils.Weather.RAIN
                    : CodecUtils.Weather.NONE;

        int yLevel = pos.getY();
        int maxYLevel = level.getMaxBuildHeight();
        int minYLevel = level.getMinBuildHeight();

        List<WeightedEntry> weightedEntries = new ArrayList<>();
        List<EntityVariantData> registryData = all(level.registryAccess());

        for (EntityVariantData data : registryData) {
            boolean matchesType = type.equals(data.type()) || (data.childType().isPresent() && type.equals(data.childType().get()));
            if (!matchesType) continue;
            if (data.spawnWeight().isEmpty()) continue;
            if (data.nameTag().isPresent()) continue;

            boolean valid = data.location().isPresent()
                    && data.location().get().matchesBiome(holder)
                    && (data.weather == CodecUtils.Weather.NONE || data.weather == weather)
                    && yLevel <= data.maxSpawnHeight().orElse(maxYLevel)
                    && yLevel > data.minSpawnHeight().orElse(minYLevel);

            if (valid) weightedEntries.add(new WeightedEntry(data.id(), data.spawnWeight().get()));
        }

        if (weightedEntries.isEmpty()) {
            for (EntityVariantData data : registryData) {
                boolean matchesType = type.equals(data.type()) || (data.childType().isPresent() && type.equals(data.childType().get()));
                if (!matchesType) continue;
                if (data.spawnWeight().isEmpty()) continue;
                if (data.nameTag().isPresent()) continue;

                boolean valid = data.location().isEmpty()
                        && (data.weather == CodecUtils.Weather.NONE || data.weather == weather)
                        && yLevel <= data.maxSpawnHeight().orElse(maxYLevel)
                        && yLevel > data.minSpawnHeight().orElse(minYLevel);

                if (valid) weightedEntries.add(new WeightedEntry(data.id(), data.spawnWeight().get()));
            }
        }

        if (weightedEntries.isEmpty()) return Optional.empty();

        int totalWeight = weightedEntries.stream().mapToInt(WeightedEntry::weight).sum();
        int randomWeight = level.getRandom().nextInt(totalWeight);
        int cumulative = 0;

        for (WeightedEntry entry : weightedEntries) {
            cumulative += entry.weight();
            if (randomWeight < cumulative) return Optional.of(entry);
        }

        return Optional.empty();
    }

    /**
     * Determines whether an Entity Variant can be used by
     * the specified Entity Type.
     * <p>
     * This checks both the variant's primary entity type and
     * its optional child entity type.
     *
     * @param type The Entity Type.
     * @param data The Entity Variant.
     * @return whether the variant supports the Entity Type.
     */
    public static boolean matchesEntityType(EntityType<?> type, EntityVariantData data) {
        return type.equals(data.type()) || (data.childType().isPresent() && type.equals(data.childType().get()));
    }

    /**
     * Represents a weighted entry used during natural
     * variant selection.
     *
     * @param id The Variant ID.
     * @param weight The selection weight of the variant.
     */
    public record WeightedEntry(ResourceLocation id, int weight) {}

    /**
     * Looks for an Entity Variant that overrides the current
     * entity based on its custom name.
     *
     * @param entity The Entity.
     * @return the matching name tag override, if one exists.
     */
    public static Optional<EntityVariantData> getNameTagOverride(Entity entity) {
        RegistryAccess access = entity.level().registryAccess();
        for (EntityVariantData data : all(access)) {
            if (matchesEntityType(entity.getType(), data)
                    && data.nameTag().isPresent()
                    && data.nameTag().get().matches(entity.getName())) {
                return Optional.of(data);
            }
        }
        return Optional.empty();
    }

    /**
     * Builds the texture location for an entity's current variant.
     * <p>
     * Name tag overrides take priority over the entity's stored variant.
     *
     * @param entity The Entity.
     * @param subfolder The subfolder inside the entity texture directory.
     * @param extra Additional text appended before the {@code .png} extension.
     * @return the Variant Texture Location.
     */
    public static ResourceLocation getVariantTexture(Entity entity, String subfolder, String extra) {
        if (!(entity instanceof JsonVariantHolder variantHolder)) return null;

        String entityName = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath();

        return getNameTagOverride(entity)
                .map(data -> ResourceLocation.fromNamespaceAndPath(
                        data.id().getNamespace(),
                        "textures/entity/" + entityName + "/" + subfolder + entityName + "_" + data.id().getPath() + extra + ".png"
                ))
                .orElseGet(() -> ResourceLocation.fromNamespaceAndPath(
                        variantHolder.getVariant().getNamespace(),
                        "textures/entity/" + entityName + "/" + subfolder + entityName + "_" + variantHolder.getVariant().getPath() + extra + ".png"
                ));
    }

    /**
     * Inserts an additional folder into an existing texture path.
     * <p>
     * This is useful for building alternate texture locations
     * while preserving the original directory structure.
     *
     * @param originalTexture The original Texture Location.
     * @param insertedNamespace The folder to insert before the texture file.
     * @param extra Additional text appended before the {@code .png} extension.
     * @return the modified Texture Location, or {@code null} if the original
     * texture is {@code null}.
     */
    public static ResourceLocation getRetextureLocation(@Nullable ResourceLocation originalTexture, String insertedNamespace, String extra) {
        if (originalTexture == null) return null;

        ArrayList<String> split = new ArrayList<>(List.of(originalTexture.getPath().split("/")));
        split.add(split.size() - 1, insertedNamespace);

        StringBuilder location = new StringBuilder();
        for (int i = 0; i < split.size(); i++) {
            if (i != 0) location.append("/");
            location.append(split.get(i));
        }

        String path = location.toString().replace(".png", "");
        return ResourceLocation.fromNamespaceAndPath(originalTexture.getNamespace(), path + extra + ".png");
    }

    /**
     * Stores the data that defines an Entity Variant.
     *
     * @param type the primary Entity Type that can use this variant.
     * @param childType an optional child Entity Type that can also use this variant.
     * @param id the unique Variant ID.
     * @param location optional biome restrictions for naturally spawning this variant.
     * @param weather the weather required for this variant to naturally spawn.
     * @param comment an optional comment ignored by the game, intended for documentation.
     * @param spawnWeight the weight used during natural variant selection.
     * @param maxSpawnHeight the maximum Y level this variant can naturally spawn at.
     * @param minSpawnHeight the minimum Y level this variant can naturally spawn at.
     * @param nameTag optional name tag rules that force this variant when an entity's
     *                custom name matches.
     * @param hidden whether this variant should be hidden from variant cycling or menus.
     * @param variantData optional Variant Data applied when this variant is assigned.
     */
    public record EntityVariantData(
            EntityType<?> type,
            Optional<EntityType<?>> childType,
            ResourceLocation id,
            Optional<CodecUtils.Location> location,
            CodecUtils.Weather weather,
            Optional<String> comment,
            Optional<Integer> spawnWeight,
            Optional<Integer> maxSpawnHeight,
            Optional<Integer> minSpawnHeight,
            Optional<CodecUtils.NameTagRule> nameTag,
            boolean hidden,
            Optional<List<VariantData>> variantData
    ) {
        public static final Codec<EntityVariantData> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("type").forGetter(EntityVariantData::type),
                        BuiltInRegistries.ENTITY_TYPE.byNameCodec().optionalFieldOf("childType").forGetter(EntityVariantData::childType),
                        ResourceLocation.CODEC.fieldOf("id").forGetter(EntityVariantData::id),
                        CodecUtils.Location.CODEC.optionalFieldOf("location").forGetter(EntityVariantData::location),
                        CodecUtils.Weather.CODEC.fieldOf("weather").orElse(CodecUtils.Weather.NONE).forGetter(EntityVariantData::weather),
                        Codec.STRING.optionalFieldOf("_comment").forGetter(EntityVariantData::comment),
                        Codec.INT.optionalFieldOf("spawnWeight").forGetter(EntityVariantData::spawnWeight),
                        Codec.INT.optionalFieldOf("maxSpawnHeight").forGetter(EntityVariantData::maxSpawnHeight),
                        Codec.INT.optionalFieldOf("minSpawnHeight").forGetter(EntityVariantData::minSpawnHeight),
                        CodecUtils.NameTagRule.CODEC.optionalFieldOf("nameTag").forGetter(EntityVariantData::nameTag),
                        Codec.BOOL.fieldOf("hidden").orElse(false).forGetter(EntityVariantData::hidden),
                        VariantData.LIST_CODEC.optionalFieldOf("variant_data_types").forGetter(EntityVariantData::variantData)
                ).apply(instance, EntityVariantData::new));
    }
}
