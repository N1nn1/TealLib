package com.ninni.teallib.api.client.renderer.variant;

import com.ninni.teallib.api.common.data.variant.VariantTextureSlot;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;

public final class VariantRenderContext {
    private static final ThreadLocal<Deque<Entity>> ENTITIES = ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<Deque<VariantTextureSlot>> SLOTS = ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<Integer> IGNORE_TEXTURE_REPLACEMENT = ThreadLocal.withInitial(() -> 0);

    private VariantRenderContext() {}

    public static void push(Entity entity) {
        ENTITIES.get().push(entity);
    }

    public static void pop() {
        Deque<Entity> stack = ENTITIES.get();

        if (!stack.isEmpty()) stack.pop();
        if (stack.isEmpty()) ENTITIES.remove();
    }

    @Nullable
    public static Entity get() {
        Deque<Entity> stack = ENTITIES.get();
        return stack.isEmpty() ? null : stack.peek();
    }

    /** Which part of the render is running, so a texture the body shares can still be told apart. */
    public enum Scope { BASE, LAYER, UNSCOPED }

    private static final ThreadLocal<Deque<Scope>> SCOPES = ThreadLocal.withInitial(ArrayDeque::new);

    public static void pushScope(Scope scope) {
        SCOPES.get().push(scope);
    }

    public static void popScope() {
        Deque<Scope> stack = SCOPES.get();

        if (!stack.isEmpty()) stack.pop();
        if (stack.isEmpty()) SCOPES.remove();
    }

    public static Scope scope() {
        Deque<Scope> stack = SCOPES.get();
        return stack.isEmpty() ? Scope.UNSCOPED : stack.peek();
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