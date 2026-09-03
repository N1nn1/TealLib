package com.ninni.teallib.core.mixin.client;

import com.ninni.teallib.api.client.renderer.variant.VariantRenderContext;
import com.ninni.teallib.api.client.renderer.variant.VariantRenderManager;
import com.ninni.teallib.api.common.data.variant.VariantTextureSlot;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.client.NeoForgeRenderTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NeoForgeRenderTypes.class)
public abstract class NeoForgeRenderTypesMixin {

    @Unique
    private static ResourceLocation teallib$texture(ResourceLocation original) {
        Entity entity = VariantRenderContext.get();
        VariantTextureSlot slot = VariantRenderContext.getSlot();
        if (entity == null || VariantRenderContext.shouldIgnoreTextureReplacement()) return original;
        if (slot == null) return VariantRenderManager.getTexture(entity, original);
        return VariantRenderManager.getTextureForSlot(entity, original, slot);
    }

    @Inject(method = "getEntityCutoutMipped", at = @At("HEAD"), cancellable = true)
    private static void teallib$getEntityCutoutMipped(ResourceLocation textureLocation, CallbackInfoReturnable<RenderType> cir) {
        Entity entity = VariantRenderContext.get();
        if (entity == null || VariantRenderContext.shouldIgnoreTextureReplacement()) return;
        cir.setReturnValue(NeoForgeRenderTypes.Internal.LAYERED_ITEM_CUTOUT_MIPPED.apply(teallib$texture(textureLocation)));
    }
    @Inject(method = "getUnsortedTranslucent", at = @At("HEAD"), cancellable = true)
    private static void teallib$getUnsortedTranslucent(ResourceLocation textureLocation, CallbackInfoReturnable<RenderType> cir) {
        Entity entity = VariantRenderContext.get();
        if (entity == null || VariantRenderContext.shouldIgnoreTextureReplacement()) return;
        cir.setReturnValue(NeoForgeRenderTypes.Internal.UNSORTED_TRANSLUCENT.apply(teallib$texture(textureLocation)));
    }
    @Inject(method = "getUnlitTranslucent(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;", at = @At("HEAD"), cancellable = true)
    private static void teallib$getUnlitTranslucent(ResourceLocation textureLocation, CallbackInfoReturnable<RenderType> cir) {
        Entity entity = VariantRenderContext.get();
        if (entity == null || VariantRenderContext.shouldIgnoreTextureReplacement()) return;
        cir.setReturnValue(NeoForgeRenderTypes.Internal.UNLIT_TRANSLUCENT_SORTED.apply(teallib$texture(textureLocation)));
    }
    @Inject(method = "getUnlitTranslucent(Lnet/minecraft/resources/ResourceLocation;Z)Lnet/minecraft/client/renderer/RenderType;", at = @At("HEAD"), cancellable = true)
    private static void teallib$getUnlitTranslucent2(ResourceLocation textureLocation, boolean sortingEnabled, CallbackInfoReturnable<RenderType> cir) {
        Entity entity = VariantRenderContext.get();
        if (entity == null || VariantRenderContext.shouldIgnoreTextureReplacement()) return;
        cir.setReturnValue((sortingEnabled ? NeoForgeRenderTypes.Internal.UNLIT_TRANSLUCENT_SORTED : NeoForgeRenderTypes.Internal.UNLIT_TRANSLUCENT_UNSORTED).apply(teallib$texture(textureLocation)));
    }
}
