package com.ninni.teallib.api.client.renderer.variant;

import com.ninni.teallib.api.common.data.variant.VariantDefinition;
import com.ninni.teallib.api.common.data.variant.VariantManager;
import com.ninni.teallib.api.common.data.variant.VariantTarget;
import com.ninni.teallib.api.common.data.variant.VariantTextureSlot;
import com.ninni.teallib.api.common.data.variant.VariantTextureSlots;
import com.ninni.teallib.api.common.data.variant.util.VariantAttachments;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class VariantRenderManager {

    private VariantRenderManager() {}

    @Nullable
    public static ResourceLocation getTexture(LivingEntity entity, ResourceLocation original) {
        return getTexture(entity, "default", original);
    }

    public static ResourceLocation getTexture(LivingEntity entity, String slot, ResourceLocation fallback) {
        Optional<VariantDefinition> variant = getCurrentVariant(entity);
        if (variant.isEmpty()) return fallback;

        Optional<ResourceLocation> texture;

        if (entity.isBaby() && slot.equals("default")) {
            texture = variant.flatMap(data -> data.texture("baby"));
            if (texture.isEmpty()) texture = variant.flatMap(data -> data.texture("default"));
        }
        else if (entity.isBaby()) {
            texture = variant.flatMap(data -> data.texture("baby_" + slot));
            if (texture.isEmpty()) texture = variant.flatMap(data -> data.texture("baby"));
            if (texture.isEmpty()) texture = variant.flatMap(data -> data.texture(slot));
            if (texture.isEmpty()) texture = variant.flatMap(data -> data.texture("default"));
        }
        else {
            texture = variant.flatMap(data -> data.texture(slot));
        }
        return texture.orElse(fallback);
    }

    @Nullable
    public static ResourceLocation getTextureForSlot(LivingEntity entity, ResourceLocation original, VariantTextureSlot slot) {
        return getTexture(entity, slot.id().getPath(), original);
    }

    public static Optional<VariantDefinition> getCurrentVariant(LivingEntity entity) {
        Optional<VariantDefinition> override = VariantManager.getNameTagOverride(entity);
        if (override.isPresent()) return override;
        return VariantAttachments.getOptional(entity).flatMap(id -> Optional.ofNullable(VariantManager.get(entity.level().registryAccess(), VariantTarget.of(entity.getType()), id)));
    }

    @Nullable
    public static VariantTextureSlot findBaseSlot(LivingEntity entity) {
        return VariantTextureSlots.findBase(entity, VariantTarget.of(entity.getType()));
    }
}