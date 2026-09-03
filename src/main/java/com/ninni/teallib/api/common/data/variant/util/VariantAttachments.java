package com.ninni.teallib.api.common.data.variant.util;

import com.ninni.teallib.api.common.data.variant.VariantManager;
import com.ninni.teallib.core.registry.TealAttachments;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
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