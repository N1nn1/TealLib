package com.ninni.teallib.common.data.entityvariant;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ninni.teallib.common.data.variantdata.VariantData;
import com.ninni.teallib.common.entity.variant.JsonVariantHolder;
import com.ninni.teallib.common.data.CodecUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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

public class EntityVariantManager {

    private EntityVariantManager() {}

    public static Registry<EntityVariantData> registry(RegistryAccess access) {
        return access.registryOrThrow(EntityVariantRegistries.ENTITY_VARIANT_REGISTRY_KEY);
    }

    public static List<EntityVariantData> all(RegistryAccess access) {
        List<EntityVariantData> out = new ArrayList<>();
        for (Map.Entry<ResourceKey<EntityVariantData>, EntityVariantData> entry : registry(access).entrySet()) {
            out.add(entry.getValue());
        }
        return out;
    }

    @Nullable
    public static EntityVariantData get(RegistryAccess access, ResourceLocation id) {
        for (EntityVariantData data : all(access)) {
            if (data.id().equals(id)) return data;
        }
        return null;
    }

    @Nullable
    public static EntityVariantData getForEntityType(RegistryAccess access, EntityType<?> type, ResourceLocation id) {
        for (EntityVariantData data : all(access)) {
            if (data.id().equals(id) && matchesEntityType(type, data)) return data;
        }
        return null;
    }

    public static int getVariantCountFor(RegistryAccess access, EntityType<?> type) {
        int count = 0;
        for (EntityVariantData data : all(access)) {
            if (matchesEntityType(type, data)) count++;
        }
        return count;
    }

    public static boolean isValidVariant(RegistryAccess access, ResourceLocation id) {
        return get(access, id) != null;
    }

    public static boolean isValidVariantForType(RegistryAccess access, EntityType<?> type, ResourceLocation id) {
        return getForEntityType(access, type, id) != null;
    }

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

    public static void applyVariantData(Entity mob, EntityVariantData data) {
        if (data == null || data.variantData.isEmpty()) return;

        Level level = mob.level();
        if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;

        RandomSource random = mob.getRandom();
        for (VariantData variantData : data.variantData.get()) {
            variantData.applyEntity(mob, serverLevel, random);
        }
    }

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

    public static boolean matchesEntityType(EntityType<?> type, EntityVariantData data) {
        return type.equals(data.type()) || (data.childType().isPresent() && type.equals(data.childType().get()));
    }

    public record WeightedEntry(ResourceLocation id, int weight) {}

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
