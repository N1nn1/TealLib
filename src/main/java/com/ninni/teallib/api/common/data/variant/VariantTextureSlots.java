package com.ninni.teallib.api.common.data.variant;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class VariantTextureSlots {
    private static final List<VariantTextureSlot> SLOTS = new ArrayList<>();

    public static VariantTextureSlot registerBase(ResourceLocation id, Supplier<? extends VariantTarget> target, Predicate<AttachmentHolder> condition) {
        return register(id, target, null, condition);
    }

    public static VariantTextureSlot registerLayer(ResourceLocation id, Supplier<? extends VariantTarget> target, Class<?> renderer, Predicate<AttachmentHolder> condition) {
        return register(id, target, renderer, condition);
    }

    private static VariantTextureSlot register(ResourceLocation id, Supplier<? extends VariantTarget> target, Class<?> renderer, Predicate<AttachmentHolder> condition) {
        VariantTextureSlot slot = new VariantTextureSlot(id, target::get, renderer, condition::test);
        SLOTS.add(slot);
        return slot;
    }

    public static List<VariantTextureSlot> getAll() {
        return List.copyOf(SLOTS);
    }

    public static List<VariantTextureSlot> getForTarget(VariantTarget target) {
        List<VariantTextureSlot> result = new ArrayList<>();

        for (VariantTextureSlot slot : SLOTS) {
            if (slot.getTarget().equals(target)) result.add(slot);
        }
        return result;
    }

    public static VariantTextureSlot find(AttachmentHolder object, VariantTarget target, Class<?> renderer) {
        for (VariantTextureSlot slot : SLOTS) {
            if (!slot.getTarget().equals(target)) continue;
            if (slot.isBaseRenderer()) continue;
            if (!slot.matchesRenderer(renderer)) continue;
            if (!slot.matches(object)) continue;

            return slot;
        }

        return null;
    }

    public static VariantTextureSlot findBase(AttachmentHolder object, VariantTarget target) {
        for (VariantTextureSlot slot : SLOTS) {

            if (!slot.getTarget().equals(target)) continue;
            if (!slot.isBaseRenderer()) continue;
            if (!slot.matches(object)) continue;

            return slot;
        }

        return null;
    }
}