package com.ninni.teallib.api.common.data.variant;

import com.ninni.teallib.api.common.data.CodecUtils;
import com.ninni.teallib.api.common.data.variant.util.VariantAttachments;
import com.ninni.teallib.core.TealLib;
import com.ninni.teallib.api.common.data.variantdata.VariantData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public final class VariantManager {
    private static final Set<Entity> AWAITING_VARIANT = ConcurrentHashMap.newKeySet();

    public static Registry<VariantDefinition> registry(RegistryAccess access) {
        return access.registryOrThrow(VariantRegistries.VARIANT_REGISTRY_KEY);
    }

    public static List<VariantDefinition> all(RegistryAccess access) {
        List<VariantDefinition> out = new ArrayList<>();
        for (Map.Entry<ResourceKey<VariantDefinition>, VariantDefinition> entry : registry(access).entrySet()) out.add(entry.getValue());
        return out;
    }

    @Nullable
    public static VariantDefinition get(RegistryAccess access, VariantTarget target, ResourceLocation id) {
        for (VariantDefinition variant : all(access)) {
            if (variant.id().equals(id) && variant.targets().contains(target)) return variant;
        }
        return null;
    }


    public static VariantTarget target(EntityType<?> type) {
        return VariantTarget.of(type);
    }
    public static VariantTarget target(BlockEntityType<?> type) {
        return VariantTarget.of(type);
    }
    public static VariantTarget target(ParticleType<?> type) {
        return VariantTarget.of(type);
    }
    public static VariantTarget customTarget(ResourceLocation id) {
        return VariantTarget.custom(id);
    }
    public static boolean supports(VariantDefinition variant, VariantTarget target) {
        return variant.supports(target);
    }

    @Nullable
    public static VariantDefinition getForTarget(RegistryAccess access, VariantTarget target, ResourceLocation id) {
        VariantDefinition variant = get(access, target, id);
        if (variant == null || !variant.supports(target)) return null;
        return variant;
    }

    @Nullable
    public static VariantDefinition getForTarget(RegistryAccess access, Entity entity) {
        if (VariantAttachments.has(entity)) return getForTarget(access, VariantTarget.of(entity.getType()), VariantAttachments.get(entity));
        return null;
    }

    @Nullable
    public static VariantDefinition getForTarget(RegistryAccess access, BlockEntity be) {
        if (VariantAttachments.has(be)) return getForTarget(access, VariantTarget.of(be.getType()), VariantAttachments.get(be));
        return null;
    }

    public static boolean isValidVariant(RegistryAccess access, VariantTarget target, ResourceLocation id) {
        return getForTarget(access, target, id) != null;
    }

    public static int getVariantCountFor(RegistryAccess access, VariantTarget target) {
        int count = 0;
        for (VariantDefinition variant : all(access)) {
            if (variant.supports(target)) count++;
        }
        return count;
    }

    public static List<VariantDefinition> getAllVariantsFor(RegistryAccess access, VariantTarget target, boolean countHidden) {
        List<VariantDefinition> list = new ArrayList<>();
        for (VariantDefinition variant : all(access)) {
            if (variant.supports(target)) {
                if (!countHidden && variant.hidden()) continue;
                list.add(variant);
            }
        }
        return list;
    }

    @Nullable
    public static ResourceLocation getNaturalVariant(VariantTarget target, Level level, BlockPos pos, RandomSource random) {
        List<WeightedEntry> candidates = candidatesFor(target, level, pos);
        WeightedEntry selected = choose(candidates, random);
        return selected != null ? selected.id() : null;
    }
    public static ResourceLocation getNaturalVariant(EntityType<?> type, Level level, BlockPos pos, RandomSource random) {
        return getNaturalVariant(VariantTarget.of(type), level, pos, random);
    }
    public static ResourceLocation getNaturalVariant(BlockEntityType<?> type, Level level, BlockPos pos, RandomSource random) {
        return getNaturalVariant(VariantTarget.of(type), level, pos, random);
    }
    public static ResourceLocation getNaturalVariant(ParticleType<?> type, Level level, BlockPos pos, RandomSource random) {
        return getNaturalVariant(VariantTarget.of(type), level, pos, random);
    }

    /**
     * Finds all variants which can naturally occur for a {@link VariantTarget}.
     * <p>
     * Biome specific variants are prioritized, if there's none for s said variant, non biome specific variants are chosen
     */
    public static List<WeightedEntry> candidatesFor(VariantTarget target, LevelReader level, BlockPos pos) {
        Holder<Biome> biome = level.getBiome(pos);
        int y = pos.getY();
        int maxY = level.getMaxBuildHeight();
        int minY = level.getMinBuildHeight();

        CodecUtils.Weather weather = resolveWeather(level, pos, biome);

        List<VariantDefinition> matching = new ArrayList<>();
        int best = Integer.MIN_VALUE;

        for (VariantDefinition data : all(level.registryAccess())) {
            if (!data.supports(target) || data.spawnWeight().isEmpty() || data.nameTag().isPresent()) continue;
            if (data.spawnWeight().get() <= 0) continue;
            if (data.location().isPresent() && !data.location().get().contains(biome)) continue;
            if (!weatherMatches(data, weather) || !heightMatches(data, y, minY, maxY)) continue;

            int priority = data.effectivePriority();
            if (priority > best) {
                best = priority;
                matching.clear();
            }
            if (priority == best) matching.add(data);
        }

        List<WeightedEntry> out = new ArrayList<>();
        for (VariantDefinition data : matching) out.add(new WeightedEntry(data.id(), data.spawnWeight().get()));
        return out;
    }

    private static List<WeightedEntry> buildWeighted(RegistryAccess access, VariantTarget target, Predicate<VariantDefinition> filter) {
        List<WeightedEntry> out = new ArrayList<>();

        for (VariantDefinition data : all(access)) {
            if (!data.supports(target) || data.spawnWeight().isEmpty() || data.nameTag().isPresent()) continue;
            int weight = data.spawnWeight().get();
            if (weight <= 0) continue;

            if (filter.test(data)) out.add(new WeightedEntry(data.id(), weight));
        }

        return out;
    }

    private static boolean heightMatches(VariantDefinition data, int y, int minY, int maxY) {
        return y <= data.maxSpawnHeight().orElse(maxY) && y > data.minSpawnHeight().orElse(minY);
    }
    private static boolean weatherMatches(VariantDefinition data, CodecUtils.Weather actual) {
        return data.weather() == CodecUtils.Weather.NONE || data.weather() == actual;
    }

    public static Optional<WeightedEntry> chooseVariant(VariantTarget target, LevelReader level, BlockPos pos) {
        List<WeightedEntry> entries = candidatesFor(target, level, pos);

        if (entries.isEmpty()) return Optional.empty();
        return Optional.ofNullable(choose(entries, RandomSource.create()));
    }

    public static WeightedEntry choose(List<WeightedEntry> entries, RandomSource random) {
        if (entries.isEmpty()) return null;

        int totalWeight = 0;

        for (WeightedEntry entry : entries) {
            if (entry.weight() > 0) totalWeight += entry.weight();
        }

        if (totalWeight <= 0) return null;
        int value = random.nextInt(totalWeight);
        int accumulated = 0;

        for (WeightedEntry entry : entries) {
            if (entry.weight() <= 0) continue;
            accumulated += entry.weight();
            if (value < accumulated) return entry;
        }

        return null;
    }


    private static CodecUtils.Weather resolveWeather(LevelReader level, BlockPos pos, Holder<Biome> biome) {
        Level weatherLevel;

        if (level instanceof Level l) weatherLevel = l;
        else if (level instanceof ServerLevelAccessor accessor) weatherLevel = accessor.getLevel();
        else return CodecUtils.Weather.NONE;

        if (!weatherLevel.isRaining()) return CodecUtils.Weather.NONE;
        if (!level.canSeeSky(pos)) return CodecUtils.Weather.NONE;
        if (level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos).getY() > pos.getY()) return CodecUtils.Weather.NONE;

        Biome.Precipitation precipitation = biome.value().getPrecipitationAt(pos);
        if (precipitation == Biome.Precipitation.SNOW) return CodecUtils.Weather.SNOW;

        if (precipitation == Biome.Precipitation.RAIN) {
            return weatherLevel.isThundering() ? CodecUtils.Weather.THUNDER : CodecUtils.Weather.RAIN;
        }

        return CodecUtils.Weather.NONE;
    }


    public static void assignNaturally(Entity entity) {
        if (!(entity.level() instanceof ServerLevelAccessor level)) {
            TealLib.LOGGER.debug("No variant for {}: not on a server level", entity.getType());
            return;
        }

        if (canQueryNow(level, entity.blockPosition())) assignNaturally(entity, level);
        else AWAITING_VARIANT.add(entity);
    }

    public static void assignNaturally(Entity entity, ServerLevelAccessor level) {
        VariantTarget target = VariantTarget.of(entity.getType());

        List<WeightedEntry> candidates = candidatesFor(target, level, entity.blockPosition());
        WeightedEntry selected = choose(candidates, RandomSource.create());
        if (selected == null) {
            TealLib.LOGGER.debug("No variant for {} at {}: {} candidates passed the spawn conditions",
                    entity.getType(), entity.blockPosition(), candidates.size());
            return;
        }

        ResourceLocation id = selected.id();
        VariantDefinition variant = getForTarget(level.registryAccess(), target, id);
        if (variant == null) {
            TealLib.LOGGER.warn("Variant {} was picked for {} but no definition carries that id", id, entity.getType());
            return;
        }

        VariantAttachments.set(entity, variant.id());
        applyVariantData(entity, level, variant);
        TealLib.LOGGER.debug("Variant {} assigned to {} at {}", variant.id(), entity.getType(), entity.blockPosition());
    }

    public static void clearAwaitingVariants() {
        AWAITING_VARIANT.clear();
    }

    public static void tickAwaitingVariants() {
        if (AWAITING_VARIANT.isEmpty()) return;
        Iterator<Entity> iterator = AWAITING_VARIANT.iterator();

        while (iterator.hasNext()) {
            Entity entity = iterator.next();

            if (entity.isRemoved() || entity.level().isClientSide) {
                iterator.remove();
                continue;
            }

            if (entity.level() instanceof ServerLevelAccessor level && canQueryNow(level, entity.blockPosition())) {
                iterator.remove();
                assignNaturally(entity, level);
            }
        }
    }

    private static boolean canQueryNow(ServerLevelAccessor level, BlockPos pos) {
        return level.getLevel().getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4) != null;
    }

    public static void assignNaturally(BlockEntity blockEntity) {
        Level level = blockEntity.getLevel();

        if (level == null) return;

        VariantTarget target = VariantTarget.of(blockEntity.getType());
        ResourceLocation id = getNaturalVariant(target, level, blockEntity.getBlockPos(), RandomSource.create());
        if (id == null) return;
        VariantDefinition variant = getForTarget(level.registryAccess(), target, id);
        if (variant == null) return;

        VariantAttachments.set(blockEntity, variant.id());
        applyVariantData(blockEntity, variant);
    }

    public static Optional<VariantDefinition> getNameTagOverride(RegistryAccess access, VariantTarget target, Component customName) {
        if (customName == null) return Optional.empty();

        for (VariantDefinition data : all(access)) {
            if (!data.supports(target)) continue;
            if (data.nameTag().isPresent() && data.nameTag().get().matches(customName)) return Optional.of(data);
        }
        return Optional.empty();
    }

    public static Optional<VariantDefinition> getNameTagOverride(Entity entity) {
        Component customName = entity.getCustomName();
        if (customName == null) return Optional.empty();
        return getNameTagOverride(entity.level().registryAccess(), VariantTarget.of(entity.getType()), customName);
    }

    public static Optional<VariantDefinition> getNameTagOverride(BlockEntity blockEntity) {
        if (!(blockEntity instanceof Nameable nameable)) return Optional.empty();
        Component customName = nameable.getCustomName();
        if (customName == null) return Optional.empty();
        Level level = blockEntity.getLevel();
        if (level == null) return Optional.empty();
        return getNameTagOverride(level.registryAccess(), VariantTarget.of(blockEntity.getType()), customName);
    }

    public static void applyVariantData(Entity entity, ServerLevelAccessor level, VariantDefinition variant) {
        if (variant == null || variant.variantData().isEmpty()) return;

        RandomSource random = entity.getRandom();

        for (VariantData data : variant.variantData().get()) {
            data.applyEntity(entity, level, random);
        }
    }

    public static void applyVariantData(BlockEntity blockEntity, VariantDefinition variant) {
        if (variant == null || variant.variantData().isEmpty()) return;
        Level level = blockEntity.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) return;

        RandomSource random = RandomSource.create();

        for (VariantData data : variant.variantData().get()) {
            data.applyBlockEntity(blockEntity, serverLevel, random);
        }
    }

    @Nullable
    public static ResourceLocation getTexture(RegistryAccess access, VariantTarget target, ResourceLocation variantId, String slot) {
        VariantDefinition variant = get(access, target, variantId);
        if (variant == null) return null;
        return variant.texture(slot).orElse(null);
    }

    @NotNull
    public static ResourceLocation getTexture(LivingEntity entity, String slot, ResourceLocation fallback) {
        Optional<VariantDefinition> override = VariantManager.getNameTagOverride(entity);
        Optional<ResourceLocation> variantId = override.map(VariantDefinition::id);

        if (variantId.isEmpty()) variantId = VariantAttachments.getOptional(entity);
        if (variantId.isEmpty()) return fallback;

        VariantDefinition variant = VariantManager.get(entity.level().registryAccess(), VariantTarget.of(entity.getType()), variantId.get());

        if (variant == null) return fallback;
        return variant.texture(slot).orElse(fallback);
    }

    @Nullable
    public static ResourceLocation getTexture(BlockEntity blockEntity, String slot) {
        Level level = blockEntity.getLevel();
        if (level == null) return null;

        Optional<VariantDefinition> override = getNameTagOverride(blockEntity);
        ResourceLocation id = override.map(VariantDefinition::id).orElseGet(() -> VariantAttachments.get(blockEntity));
        return getTexture(level.registryAccess(), VariantTarget.of(blockEntity.getType()), id, slot);
    }

    public record WeightedEntry(ResourceLocation id, int weight) {}
}