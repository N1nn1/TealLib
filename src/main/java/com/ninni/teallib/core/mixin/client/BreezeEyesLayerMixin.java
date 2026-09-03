package com.ninni.teallib.core.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ninni.teallib.api.client.renderer.variant.VariantRenderContext;
import com.ninni.teallib.api.client.renderer.variant.VariantRenderManager;
import com.ninni.teallib.api.common.data.variant.VariantTextureSlot;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.BreezeEyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Its RenderType is a class-load constant, so the buffer is the only point a variant is in scope. */
@Mixin(BreezeEyesLayer.class)
public abstract class BreezeEyesLayerMixin {

    private static final ResourceLocation TEALLIB$VANILLA_EYES = ResourceLocation.withDefaultNamespace("textures/entity/breeze/breeze_eyes.png");

    @WrapOperation(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;")
    )
    private com.mojang.blaze3d.vertex.VertexConsumer teallib$breezeEyes(MultiBufferSource source, RenderType renderType, Operation<com.mojang.blaze3d.vertex.VertexConsumer> original) {
        Entity entity = VariantRenderContext.get();
        if (entity == null || VariantRenderContext.shouldIgnoreTextureReplacement()) return original.call(source, renderType);

        VariantTextureSlot slot = VariantRenderContext.getSlot();
        ResourceLocation replaced = slot != null
                ? VariantRenderManager.getTextureForSlot(entity, TEALLIB$VANILLA_EYES, slot)
                : VariantRenderManager.getTexture(entity, TEALLIB$VANILLA_EYES);

        if (replaced == null || replaced.equals(TEALLIB$VANILLA_EYES)) return original.call(source, renderType);
        return original.call(source, RenderType.breezeEyes(replaced));
    }
}
