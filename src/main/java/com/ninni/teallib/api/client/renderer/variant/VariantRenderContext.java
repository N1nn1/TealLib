package com.ninni.teallib.api.client.renderer.variant;

import com.ninni.teallib.api.common.data.variant.VariantTextureSlot;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;

public final class VariantRenderContext {
    private static final ThreadLocal<Deque<LivingEntity>> ENTITIES = ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<Deque<VariantTextureSlot>> SLOTS = ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<Integer> IGNORE_TEXTURE_REPLACEMENT = ThreadLocal.withInitial(() -> 0);

    private VariantRenderContext() {}

    public static void push(LivingEntity entity) {
        ENTITIES.get().push(entity);
    }

    public static void pop() {
        Deque<LivingEntity> stack = ENTITIES.get();

        if (!stack.isEmpty()) stack.pop();
        if (stack.isEmpty()) ENTITIES.remove();
    }

    @Nullable
    public static LivingEntity get() {
        Deque<LivingEntity> stack = ENTITIES.get();
        return stack.isEmpty() ? null : stack.peek();
    }

    public static void pushSlot(VariantTextureSlot slot) {
        SLOTS.get().push(slot);
    }

    public static void popSlot() {
        Deque<VariantTextureSlot> stack = SLOTS.get();

        if (!stack.isEmpty()) stack.pop();
        if (stack.isEmpty()) SLOTS.remove();
    }

    @Nullable
    public static VariantTextureSlot getSlot() {
        Deque<VariantTextureSlot> stack = SLOTS.get();
        return stack.isEmpty() ? null : stack.peek();
    }

    public static void pushIgnoreTextureReplacement() {
        IGNORE_TEXTURE_REPLACEMENT.set(
                IGNORE_TEXTURE_REPLACEMENT.get() + 1
        );
    }

    public static void popIgnoreTextureReplacement() {
        int value = IGNORE_TEXTURE_REPLACEMENT.get();

        if (value <= 1) {
            IGNORE_TEXTURE_REPLACEMENT.remove();
        } else {
            IGNORE_TEXTURE_REPLACEMENT.set(value - 1);
        }
    }

    public static boolean shouldIgnoreTextureReplacement() {
        return IGNORE_TEXTURE_REPLACEMENT.get() > 0;
    }
}