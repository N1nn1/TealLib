package com.ninni.teallib.api.client.renderer.variant;

import com.ninni.teallib.api.common.data.variant.VariantDefinition;
import com.ninni.teallib.api.common.data.variant.VariantManager;
import com.ninni.teallib.api.common.data.variant.VariantTarget;
import com.ninni.teallib.api.common.data.variant.VariantTextureSlot;
import com.ninni.teallib.api.common.data.variant.VariantTextureSlots;
import com.ninni.teallib.api.common.data.variant.util.VariantAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class VariantRenderManager {

    private VariantRenderManager() {}

    @Nullable
    public static ResourceLocation getTexture(Entity entity, ResourceLocation original) {
        return getTexture(entity, "default", original);
    }

    public static ResourceLocation getTexture(Entity entity, String slot, ResourceLocation fallback) {
        return getTexture(entity, slot, fallback, true);
    }

    /** @param bodyFallback whether an undefined slot may borrow the body texture. */
    public static ResourceLocation getTexture(Entity entity, String slot, ResourceLocation fallback, boolean bodyFallback) {
        Optional<VariantDefinition> variant = getCurrentVariant(entity);
        if (variant.isEmpty()) return fallback;

        boolean baby = entity instanceof LivingEntity living && living.isBaby();
        boolean inLayer = VariantRenderContext.scope() == VariantRenderContext.Scope.LAYER;
        String key = "default".equals(slot) ? null : slot;
        Optional<ResourceLocation> texture = Optional.empty();

        if (key != null) {
            if (baby) texture = variant.flatMap(data -> data.texture("baby_" + key));
            if (texture.isEmpty()) texture = variant.flatMap(data -> data.texture(key));
        }

        boolean body = bodyFallback && !variant.get().keepVanillaTexture();

        if (texture.isEmpty() && body && (!inLayer || isBodyTexture(entity, fallback))) {
            if (baby) texture = variant.flatMap(data -> data.texture("baby"));
            if (texture.isEmpty()) texture = variant.flatMap(data -> data.texture("default"));
        }

        if (texture.isEmpty()) texture = variant.flatMap(data -> data.overrideFor(fallback, baby));
        return texture.orElse(fallback);
    }

    /** Whether a layer draws with the mob's own skin, as warden tendrils and the slime shell do. */
    @SuppressWarnings("unchecked")
    private static boolean isBodyTexture(Entity entity, ResourceLocation candidate) {
        if (candidate == null) return false;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getEntityRenderDispatcher() == null) return false;

        EntityRenderer<Entity> renderer = (EntityRenderer<Entity>) minecraft.getEntityRenderDispatcher().getRenderer(entity);
        return renderer != null && candidate.equals(renderer.getTextureLocation(entity));
    }

    @Nullable
    public static ResourceLocation getTextureForSlot(Entity entity, ResourceLocation original, VariantTextureSlot slot) {
        return getTexture(entity, slot.id().getPath(), original, slot.isBaseRenderer());
    }

    public static Optional<VariantDefinition> getCurrentVariant(Entity entity) {
        Optional<VariantDefinition> override = VariantManager.getNameTagOverride(entity);
        if (override.isPresent()) return override;
        return VariantAttachments.getOptional(entity).flatMap(id -> Optional.ofNullable(VariantManager.get(entity.level().registryAccess(), VariantTarget.of(entity.getType()), id)));
    }

    @Nullable
    public static VariantTextureSlot findBaseSlot(Entity entity) {
        return VariantTextureSlots.findBase(entity, VariantTarget.of(entity.getType()));
    }
}