package com.ninni.teallib.api.common.data.blockvariant;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ninni.teallib.api.common.block.entity.VariantHolderBlockEntity;
import com.ninni.teallib.api.common.data.variantdata.VariantData;
import com.ninni.teallib.api.common.data.CodecUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.fml.ModList;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

/**
 * A utility class for working with {@link BlockVariantData Block Variant Data}.
 * <p>
 * It provides methods for querying registered variants, selecting naturally
 * occurring variants, applying
 * {@link com.ninni.teallib.api.common.data.variantdata.VariantData Variant Data},
 * and resolving name tag overrides for
 * {@link com.ninni.teallib.api.common.block.entity.VariantHolderBlockEntity Variant Holder Block Entities}.
 * <p>
 * Variant selection supports biome restrictions, spawn height,
 * weighted random selection and name tag overrides.
 */
public class BlockVariantManager {

    private BlockVariantManager() {}

    /**
     * @param access The Registry Access.
     * @return the Block Variant registry.
     */
    private static Registry<BlockVariantData> registry(RegistryAccess access) {
        return access.registryOrThrow(BlockVariantRegistries.BLOCK_VARIANT_REGISTRY_KEY);
    }

    /**
     * @param access The Registry Access.
     * @return every registered Block Variant.
     */
    private static List<BlockVariantData> all(RegistryAccess access) {
        List<BlockVariantData> out = new ArrayList<>();
        for (Map.Entry<ResourceKey<BlockVariantData>, BlockVariantData> entry : registry(access).entrySet()) {
            out.add(entry.getValue());
        }
        return out;
    }

    /**
     * Retrieves a Block Variant by its ID.
     *
     * @param access The Registry Access.
     * @param id The Variant ID.
     * @return the matching Block Variant, or {@code null} if none exists.
     */
    @Nullable
    public static BlockVariantData get(RegistryAccess access, ResourceLocation id) {
        for (BlockVariantData data : all(access)) {
            if (data.id().equals(id)) return data;
        }
        return null;
    }

    /**
     * Counts the number of variants registered for a Block Entity Type.
     *
     * @param access The Registry Access.
     * @param type The Block Entity Type.
     * @return the amount of registered variants.
     */
    public static int getVariantCountFor(RegistryAccess access, BlockEntityType<?> type) {
        int count = 0;
        for (BlockVariantData data : all(access)) {
            if (data == null) continue;
            if (data.type().equals(type)) count++;
        }
        return count;
    }

    /**
     * Retrieves the index assigned to a Block Variant.
     *
     * @param access The Registry Access.
     * @param resourceLocation The Variant ID.
     * @return the variant index, or {@code 0} if the variant does not exist.
     */
    public static int getIndex(RegistryAccess access, ResourceLocation resourceLocation) {
        return get(access, resourceLocation) == null ? 0 : Objects.requireNonNull(get(access, resourceLocation)).index();
    }
    public static Integer getIndex(RegistryAccess access, ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY).copyTag();

        if (tag.contains("id")) {
            for (BlockVariantManager.BlockVariantData data : all(access)) {
                if (data.type() == BuiltInRegistries.BLOCK_ENTITY_TYPE.get(ResourceLocation.parse(tag.getString("id"))) && data.nameTag().isPresent()) {
                    if (data.nameTag().get().matches(stack.getHoverName())) return data.index();
                }
            }
        }
        return null;
    }

    /**
     * Determines whether a Variant ID is valid for the specified
     * Variant Holder Block Entity.
     *
     * @param blockEntity The Variant Holder Block Entity.
     * @param id The Variant ID.
     * @return whether the variant exists for the Block Entity's type.
     */
    public static boolean isValidVariant(VariantHolderBlockEntity blockEntity, ResourceLocation id) {
        if (id == null) return false;
        Level level = blockEntity.getLevel();
        if (level == null) return false;
        for (BlockVariantData d : all(level.registryAccess())) {
            if (d.type() == blockEntity.getVariantType() && d.id().equals(id)) return true;
        }
        return false;
    }

    /**
     * Selects and assigns a naturally occurring variant to a
     * Variant Holder Block Entity.
     * <p>
     * The selected variant depends on the current biome,
     * spawn height and configured spawn weights.
     *
     * @param blockEntity The Variant Holder Block Entity.
     */
    public static void assignNaturally(VariantHolderBlockEntity blockEntity) {
        Level level = blockEntity.getLevel();
        if (level != null) {
            BlockVariantData data = get(level.registryAccess(), getNaturalVariant(blockEntity.getVariantType(), level, blockEntity.getBlockPos(), level.random, blockEntity.getDefaultVariant()));
            if (data != null) {
                applyVariantData(blockEntity, data);
                blockEntity.setVariant(data.id);
            }
        }
    }

    /**
     * Applies every configured {@link VariantData Variant Data}
     * contained within a Block Variant.
     *
     * @param be The Block Entity to apply Variant Data to.
     * @param data The Block Variant containing the Variant Data.
     */
    public static void applyVariantData(BlockEntity be, BlockVariantData data) {
        if (data == null || data.variantData.isEmpty()) return;
        Level level = be.getLevel();
        if (level instanceof ServerLevel serverLevel) {
            RandomSource random;
            if (ModList.get().isLoaded("c2me")) random = RandomSource.create();
            else random = level.getRandom();
            for (VariantData variantData : data.variantData.get()) {
                variantData.applyBlockEntity(be, serverLevel, random);
            }
        }
    }

    /**
     * Chooses a naturally occurring variant for a Block Entity Type.
     *
     * @param type The Block Entity Type.
     * @param level The Level.
     * @param pos The Block Position.
     * @param random The Random Source used for selection.
     * @param fallback The Variant ID returned if no valid variants exist.
     * @return the chosen Variant ID, or the fallback Variant ID.
     */
    public static ResourceLocation getNaturalVariant(BlockEntityType<?> type, Level level, BlockPos pos, RandomSource random, ResourceLocation fallback) {
        List<Weighted> entries = candidatesFor(type, level, pos);
        Weighted chosen = choose(entries, random);
        return chosen != null ? chosen.id() : fallback;
    }

    /**
     * Collects every valid weighted variant for a Block Entity Type
     * at the specified position.
     * <p>
     * Variants are filtered using biome and spawn height restrictions.
     * If no biome-specific variants match, variants without biome
     * restrictions are considered instead.
     *
     * @param type The Block Entity Type.
     * @param level The Level.
     * @param pos The Block Position.
     * @return every valid weighted candidate.
     */
    public static List<Weighted> candidatesFor(BlockEntityType<?> type, Level level, BlockPos pos) {
        if (level == null) return List.of();

        Holder<Biome> biome = level.getBiome(pos);
        int y = pos.getY();
        int maxY = level.getMaxBuildHeight();
        int minY = level.getMinBuildHeight();

        List<Weighted> filtered = buildWeighted(level.registryAccess(), type, data ->
                data.location().isPresent()
                        && data.location().get().matchesBiome(biome)
                        && y <= data.maxSpawnHeight().orElse(maxY)
                        && y > data.minSpawnHeight().orElse(minY)
        );

        if (filtered.isEmpty()) {
            filtered = buildWeighted(level.registryAccess(), type, data ->
                    data.location().isEmpty()
                            && y <= data.maxSpawnHeight().orElse(maxY)
                            && y > data.minSpawnHeight().orElse(minY)
            );
        }

        return filtered;
    }

    /**
     * Builds a list of weighted variants matching the supplied filter.
     *
     * @param access The Registry Access.
     * @param type The Block Entity Type.
     * @param filter The filter applied to registered variants.
     * @return every matching weighted variant.
     */
    private static List<Weighted> buildWeighted(RegistryAccess access, BlockEntityType<?> type, Predicate<BlockVariantData> filter) {
        List<Weighted> out = new ArrayList<>();
        for (BlockVariantData d : all(access)) {
            if (d.type() == type && filter.test(d) && d.spawnWeight().isPresent()) {
                out.add(new Weighted(d.id(), d.spawnWeight().get()));
            }
        }
        return out;
    }

    /**
     * Looks for a Block Variant that overrides the current
     * Block Entity based on its custom name.
     *
     * @param access The Registry Access.
     * @param be The Variant Holder Block Entity.
     * @return the matching name tag override, if one exists.
     */
    public static Optional<BlockVariantData> getNameTagOverride(RegistryAccess access, VariantHolderBlockEntity be) {
        Component customName = be.getCustomName();
        if (customName == null) return Optional.empty();
        return getNameTagOverride(access, customName, be.getVariantType());
    }

    /**
     * Looks for a Block Variant matching a custom name.
     *
     * @param access The Registry Access.
     * @param customName The custom name to test.
     * @param type The Block Entity Type.
     * @return the matching Block Variant, if one exists.
     */
    public static Optional<BlockVariantData> getNameTagOverride(RegistryAccess access, Component customName, BlockEntityType<?> type) {

        for (BlockVariantData data : all(access)) {
            if (data.type() == type && data.nameTag().isPresent()) {
                if (data.nameTag().get().matches(customName)) return Optional.of(data);
            }
        }
        return Optional.empty();
    }

    /**
     * Selects a weighted entry using weighted random selection.
     *
     * @param entries The weighted entries.
     * @param random The Random Source.
     * @return the selected weighted entry, or {@code null} if no valid
     * entries exist.
     */
    public static Weighted choose(List<Weighted> entries, RandomSource random) {
        if (entries.isEmpty()) return null;
        int total = entries.stream().mapToInt(Weighted::weight).sum();
        if (total <= 0) return null;
        int r = random.nextInt(total);
        int acc = 0;
        for (Weighted e : entries) { acc += e.weight(); if (r < acc) return e; }
        return null;
    }

    public record Weighted(ResourceLocation id, int weight) {}

    /**
     * Stores the data that defines a Block Variant.
     *
     * @param type the Block Entity Type that can use this variant.
     * @param id the unique Variant ID.
     * @param index the variant index, typically used for cycling variants or
     *              displaying them in a consistent order.
     * @param location optional biome restrictions for naturally selecting this variant.
     * @param spawnWeight the weight used during natural variant selection.
     * @param maxSpawnHeight the maximum Y level this variant can naturally occur at.
     * @param minSpawnHeight the minimum Y level this variant can naturally occur at.
     * @param nameTag optional name tag rules that force this variant when a Block Entity's
     *                custom name matches.
     * @param variantData optional Variant Data applied when this variant is assigned.
     */
    public record BlockVariantData(
            BlockEntityType<?> type,
            ResourceLocation id,
            int index,
            Optional<CodecUtils.Location> location,
            Optional<Integer> spawnWeight,
            Optional<Integer> maxSpawnHeight,
            Optional<Integer> minSpawnHeight,
            Optional<CodecUtils.NameTagRule> nameTag,
            Optional<List<VariantData>> variantData
    ) {

        public static final Codec<BlockVariantData> CODEC = RecordCodecBuilder.create(i -> i.group(
                BuiltInRegistries.BLOCK_ENTITY_TYPE.byNameCodec().fieldOf("type").forGetter(BlockVariantData::type),
                ResourceLocation.CODEC.fieldOf("id").forGetter(BlockVariantData::id),
                Codec.INT.fieldOf("index").orElse(0).forGetter(BlockVariantData::index),
                CodecUtils.Location.CODEC.optionalFieldOf("location").forGetter(BlockVariantData::location),
                Codec.INT.optionalFieldOf("spawnWeight").forGetter(BlockVariantData::spawnWeight),
                Codec.INT.optionalFieldOf("maxSpawnHeight").forGetter(BlockVariantData::maxSpawnHeight),
                Codec.INT.optionalFieldOf("minSpawnHeight").forGetter(BlockVariantData::minSpawnHeight),
                CodecUtils.NameTagRule.CODEC.optionalFieldOf("nameTag").forGetter(BlockVariantData::nameTag),
                VariantData.LIST_CODEC.optionalFieldOf("variant_data_types").forGetter(BlockVariantData::variantData)
        ).apply(i, BlockVariantData::new));
    }
}
