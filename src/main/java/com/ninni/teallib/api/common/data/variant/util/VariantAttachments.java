package com.ninni.teallib.api.common.data.variant.util;

import com.ninni.teallib.api.common.data.variant.VariantDefinition;
import com.ninni.teallib.api.common.data.variant.VariantManager;
import com.ninni.teallib.api.common.data.variant.VariantTarget;
import com.ninni.teallib.core.TealLib;
import com.ninni.teallib.core.registry.TealAttachments;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

import javax.annotation.Nullable;
import java.util.Optional;

public final class VariantAttachments {

    public static Optional<ResourceLocation> get(IAttachmentHolder holder) {
        return holder.getData(TealAttachments.VARIANT);
    }
    public static boolean has(IAttachmentHolder holder) {
        return get(holder).isPresent();
    }
    public static void set(IAttachmentHolder holder, ResourceLocation id) {
        holder.setData(TealAttachments.VARIANT, Optional.ofNullable(id));
    }

    public static void clear(IAttachmentHolder holder) {
        holder.removeData(TealAttachments.VARIANT);
    }

    public static Optional<ResourceLocation> getOptional(Entity entity) {
        return get((IAttachmentHolder) entity);
    }
    public static Optional<ResourceLocation> getOptional(BlockEntity blockEntity) {
        return get((IAttachmentHolder) blockEntity);
    }

    @Nullable
    public static ResourceLocation get(Entity entity) {
        return getOptional(entity).orElse(null);
    }
    @Nullable
    public static ResourceLocation get(BlockEntity blockEntity) {
        return getOptional(blockEntity).orElse(null);
    }

    public static void set(Entity entity, ResourceLocation id) {
        set((IAttachmentHolder) entity, id);
    }
    public static void set(BlockEntity blockEntity, ResourceLocation id) {
        set((IAttachmentHolder) blockEntity, id);
    }

    public static void clear(Entity entity) {
        clear((IAttachmentHolder) entity);
    }
    public static void clear(BlockEntity blockEntity) {
        clear((IAttachmentHolder) blockEntity);
    }

    @Nullable
    public static Entity getRandomBabyWithVariant(EntityType<?> type, ServerLevel serverLevel, Entity parentA, @Nullable Entity parentB) {
        Entity baby = type.create(serverLevel);
        if (baby != null) setRandomBabyVariant(serverLevel, parentA, parentB, baby);
        return baby;
    }

    @Nullable
    public static AgeableMob getRandomAgeableBabyWithVariant(EntityType<?> type, ServerLevel serverLevel, Entity parentA, @Nullable Entity parentB) {
        Entity baby = getRandomBabyWithVariant(type, serverLevel, parentA, parentB);
        if (baby instanceof AgeableMob ageableMob) return ageableMob;
        return null;
    }

    public static void setRandomBabyVariant(ServerLevel serverLevel, Entity parentA, @Nullable Entity parentB, Entity baby) {
        if (isEntityValid(baby.getType())) {
            baby.setPos(parentA.position());
            boolean bExists = parentB != null && VariantAttachments.has(parentB);
            boolean aExists = VariantAttachments.has(parentA);
            if ((aExists || bExists) && serverLevel.random.nextBoolean()) {
                ResourceLocation variant;

                if (parentB != null && VariantAttachments.has(parentB) && aExists) {
                    if (serverLevel.random.nextBoolean()) variant = VariantAttachments.get(parentA);
                    else variant = VariantAttachments.get(parentB);
                } else {
                    if (aExists) variant = VariantAttachments.get(parentA);
                    else variant = VariantAttachments.get(parentB);
                }

                VariantTarget target = VariantTarget.of(baby.getType());
                VariantDefinition forTarget = VariantManager.getForTarget(serverLevel.registryAccess(), target, variant);
                if (forTarget != null) {
                    if (VariantManager.supports(forTarget, target)) {
                        VariantAttachments.set(baby, variant);
                    }
                }
            } else VariantManager.assignNaturally(baby, serverLevel);
        }
    }

    public static boolean isEntityValid(EntityType<?> entityType) {
        ResourceLocation type = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        if (TealLib.COMMON_CONFIG.variantNamespaceBlacklist.get().contains(type.getNamespace())) return false;
        boolean contains = false;
        for (String string : TealLib.COMMON_CONFIG.variantBlacklist.get()) {
            if (type.toString().equals(string)) {
                contains = true;
                break;
            }
        }
        return !contains;
    }

    public static void toTag(Entity entity, CompoundTag tag) {
        if (VariantAttachments.has(entity)) tag.putString("Variant", VariantAttachments.get(entity).toString());
    }

    public static void fromTag(Entity entity, CompoundTag tag, boolean tryAssign) {
        if (tag.contains("Variant")) {
            ResourceLocation variant = ResourceLocation.tryParse(tag.getString("Variant"));
            if (variant != null) VariantAttachments.set(entity, variant);
            else {
                if (tryAssign) VariantManager.assignNaturally(entity);
            }
        }
    }
}