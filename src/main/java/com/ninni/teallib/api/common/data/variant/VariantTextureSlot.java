package com.ninni.teallib.api.common.data.variant;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentHolder;

import java.util.function.Supplier;

/**
 * Identifies a texture slot used by a variant.
 *
 * @param id The slot ID stored in VariantDefinition textures.
 * @param target The target this slot belongs to.
 * @param renderer The renderer or render layer this slot is associated with.
 * @param condition The condition under which this slot should be used.
 */
public record VariantTextureSlot(
        ResourceLocation id,
        Supplier<VariantTarget> target,
        Class<?> renderer,
        VariantTextureCondition<?> condition
) {

    public VariantTarget getTarget() {
        return target.get();
    }

    public boolean matchesRenderer(Class<?> rendererClass) {
        return renderer == null || renderer.isAssignableFrom(rendererClass);
    }

    public boolean matches(AttachmentHolder object) {
        return condition.test(object);
    }

    public boolean isBaseRenderer() {
        return renderer == null;
    }

    @FunctionalInterface
    public interface VariantTextureCondition<T extends AttachmentHolder> {
        boolean test(AttachmentHolder object);
    }
}