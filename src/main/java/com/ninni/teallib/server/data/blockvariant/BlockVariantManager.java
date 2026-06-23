package com.ninni.teallib.server.data.blockvariant;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ninni.teallib.server.block.entity.VariantHolderBlockEntity;
import com.ninni.teallib.server.data.variantdata.VariantData;
import com.ninni.teallib.server.entity.JsonVariantHolder;
import com.ninni.teallib.util.CodecUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
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

public class BlockVariantManager {

    private BlockVariantManager() {}

    private static Registry<BlockVariantData> registry(RegistryAccess access) {
        return access.registryOrThrow(BlockVariantRegistries.BLOCK_VARIANT_REGISTRY_KEY);
    }

    private static List<BlockVariantData> all(RegistryAccess access) {
        List<BlockVariantData> out = new ArrayList<>();
        for (Map.Entry<ResourceKey<BlockVariantData>, BlockVariantData> entry : registry(access).entrySet()) {
            out.add(entry.getValue());
        }
        return out;
    }

    @Nullable
    public static BlockVariantData get(RegistryAccess access, ResourceLocation id) {
        for (BlockVariantData data : all(access)) {
            if (data.id().equals(id)) return data;
        }
        return null;
    }


    public static int getVariantCountFor(RegistryAccess access, BlockEntityType<?> type) {
        int count = 0;
        for (BlockVariantData data : all(access)) {
            if (data == null) continue;
            if (data.type().equals(type)) count++;
        }
        return count;
    }

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

    public static boolean isValidVariant(VariantHolderBlockEntity blockEntity, ResourceLocation id) {
        if (id == null) return false;
        Level level = blockEntity.getLevel();
        if (level == null) return false;
        for (BlockVariantData d : all(level.registryAccess())) {
            if (d.type() == blockEntity.getVariantType() && d.id().equals(id)) return true;
        }
        return false;
    }

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

    public static ResourceLocation getNaturalVariant(BlockEntityType<?> type, Level level, BlockPos pos, RandomSource random, ResourceLocation fallback) {
        List<Weighted> entries = candidatesFor(type, level, pos);
        Weighted chosen = choose(entries, random);
        return chosen != null ? chosen.id() : fallback;
    }

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

    private static List<Weighted> buildWeighted(RegistryAccess access, BlockEntityType<?> type, Predicate<BlockVariantData> filter) {
        List<Weighted> out = new ArrayList<>();
        for (BlockVariantData d : all(access)) {
            if (d.type() == type && filter.test(d) && d.spawnWeight().isPresent()) {
                out.add(new Weighted(d.id(), d.spawnWeight().get()));
            }
        }
        return out;
    }

    public static Optional<BlockVariantData> getNameTagOverride(RegistryAccess access, VariantHolderBlockEntity be) {
        Component customName = be.getCustomName();
        if (customName == null) return Optional.empty();
        return getNameTagOverride(access, customName, be.getVariantType());
    }

    public static Optional<BlockVariantData> getNameTagOverride(RegistryAccess access, Component customName, BlockEntityType<?> type) {

        for (BlockVariantData data : all(access)) {
            if (data.type() == type && data.nameTag().isPresent()) {
                if (data.nameTag().get().matches(customName)) return Optional.of(data);
            }
        }
        return Optional.empty();
    }


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
