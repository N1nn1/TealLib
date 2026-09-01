package com.ninni.teallib.api.common.data.variant.util;

import com.ninni.teallib.api.common.data.variant.VariantTarget;
import com.ninni.teallib.api.common.data.variant.VariantTextureSlot;
import com.ninni.teallib.api.common.data.variant.VariantTextureSlots;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.attachment.AttachmentHolder;

import java.util.function.Predicate;
import java.util.function.Supplier;

public final class VariantTextureRegistry {

    public static VariantTextureSlot registerBase(ResourceLocation id, Supplier<? extends VariantTarget> target, Predicate<AttachmentHolder> condition) {
        return VariantTextureSlots.registerBase(id, target, condition);
    }
    public static VariantTextureSlot registerBase(ResourceLocation id, Supplier<? extends VariantTarget> target) {
        return VariantTextureSlots.registerBase(id, target, (attachmentHolder -> true));
    }
    public static VariantTextureSlot registerLayer(ResourceLocation id, Supplier<? extends VariantTarget> target, Class<?> renderer, Predicate<AttachmentHolder> condition) {
        return VariantTextureSlots.registerLayer(id, target, renderer, condition);
    }
    public static VariantTextureSlot registerLayer(ResourceLocation id, Supplier<? extends VariantTarget> target, Class<?> renderer) {
        return VariantTextureSlots.registerLayer(id, target, renderer, (attachmentHolder -> true));
    }

    public static VariantTextureSlot registerBase(ResourceLocation id, EntityType<?> entityType, Predicate<AttachmentHolder> condition) {
        return registerBase(id, () -> VariantTarget.of(entityType), condition);
    }
    public static VariantTextureSlot registerBase(ResourceLocation id, EntityType<?> entityType) {
        return registerBase(id, () -> VariantTarget.of(entityType), (attachmentHolder -> true));
    }
    public static VariantTextureSlot registerLayer(ResourceLocation id, EntityType<?> entityType, Class<?> renderer, Predicate<AttachmentHolder> condition) {
        return registerLayer(id, () -> VariantTarget.of(entityType), renderer, condition);
    }
    public static VariantTextureSlot registerLayer(ResourceLocation id, EntityType<?> entityType, Class<?> renderer) {
        return registerLayer(id, () -> VariantTarget.of(entityType), renderer, (attachmentHolder -> true));
    }

    public static VariantTextureSlot registerBase(ResourceLocation id, BlockEntityType<?> blockEntityType, Predicate<AttachmentHolder> condition) {
        return registerBase(id, () -> VariantTarget.of(blockEntityType), condition);
    }
    public static VariantTextureSlot registerBase(ResourceLocation id, BlockEntityType<?> blockEntityType) {
        return registerBase(id, () -> VariantTarget.of(blockEntityType), (attachmentHolder -> true));
    }
    public static VariantTextureSlot registerLayer(ResourceLocation id, BlockEntityType<?> blockEntityType, Class<?> renderer, Predicate<AttachmentHolder> condition) {
        return registerLayer(id, () -> VariantTarget.of(blockEntityType), renderer, condition);
    }
    public static VariantTextureSlot registerLayer(ResourceLocation id, BlockEntityType<?> blockEntityType, Class<?> renderer) {
        return registerLayer(id, () -> VariantTarget.of(blockEntityType), renderer, (attachmentHolder -> true));
    }

    public static VariantTextureSlot registerBase(ResourceLocation id, ParticleType<?> particleType, Predicate<AttachmentHolder> condition) {
        return registerBase(id, () -> VariantTarget.of(particleType), condition);
    }
    public static VariantTextureSlot registerBase(ResourceLocation id, ParticleType<?> particleType) {
        return registerBase(id, () -> VariantTarget.of(particleType), (attachmentHolder -> true));
    }
    public static VariantTextureSlot registerLayer(ResourceLocation id, ParticleType<?> particleType, Class<?> renderer, Predicate<AttachmentHolder> condition) {
        return registerLayer(id, () -> VariantTarget.of(particleType), renderer, condition);
    }
    public static VariantTextureSlot registerLayer(ResourceLocation id, ParticleType<?> particleType, Class<?> renderer) {
        return registerLayer(id, () -> VariantTarget.of(particleType), renderer, (attachmentHolder -> true));
    }
}
