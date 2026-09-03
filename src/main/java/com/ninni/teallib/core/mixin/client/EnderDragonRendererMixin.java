package com.ninni.teallib.core.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ninni.teallib.api.client.renderer.variant.VariantRenderContext;
import com.ninni.teallib.api.client.renderer.variant.VariantRenderManager;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Its render types are class-load constants, so the buffer is the only reachable point. Rebuilt
 * factories are matched by identity, which needs substitution off while the comparison runs.
 */
@Mixin(EnderDragonRenderer.class)
public abstract class EnderDragonRendererMixin {

    private static final ResourceLocation TEALLIB$BODY = ResourceLocation.withDefaultNamespace("textures/entity/enderdragon/dragon.png");
    private static final ResourceLocation TEALLIB$EYES = ResourceLocation.withDefaultNamespace("textures/entity/enderdragon/dragon_eyes.png");
    private static final ResourceLocation TEALLIB$EXPLODING = ResourceLocation.withDefaultNamespace("textures/entity/enderdragon/dragon_exploding.png");

    @WrapOperation(
            method = "render(Lnet/minecraft/world/entity/boss/enderdragon/EnderDragon;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;")
    )
    private VertexConsumer teallib$dragonBuffer(MultiBufferSource source, RenderType renderType, Operation<VertexConsumer> original) {
        Entity entity = VariantRenderContext.get();
        if (entity == null || VariantRenderContext.shouldIgnoreTextureReplacement()) return original.call(source, renderType);

        RenderType body;
        RenderType decal;
        RenderType eyes;
        RenderType exploding;
        VariantRenderContext.pushIgnoreTextureReplacement();
        try {
            body = RenderType.entityCutoutNoCull(TEALLIB$BODY);
            decal = RenderType.entityDecal(TEALLIB$BODY);
            eyes = RenderType.eyes(TEALLIB$EYES);
            exploding = RenderType.dragonExplosionAlpha(TEALLIB$EXPLODING);
        } finally {
            VariantRenderContext.popIgnoreTextureReplacement();
        }

        ResourceLocation source_ = null;
        if (renderType == body || renderType == decal) source_ = TEALLIB$BODY;
        else if (renderType == eyes) source_ = TEALLIB$EYES;
        else if (renderType == exploding) source_ = TEALLIB$EXPLODING;
        if (source_ == null) return original.call(source, renderType);

        ResourceLocation replaced = VariantRenderManager.getTexture(entity, source_);
        if (replaced == null || replaced.equals(source_)) return original.call(source, renderType);

        RenderType swapped;
        VariantRenderContext.pushIgnoreTextureReplacement();
        try {
            if (renderType == body) swapped = RenderType.entityCutoutNoCull(replaced);
            else if (renderType == decal) swapped = RenderType.entityDecal(replaced);
            else if (renderType == eyes) swapped = RenderType.eyes(replaced);
            else swapped = RenderType.dragonExplosionAlpha(replaced);
        } finally {
            VariantRenderContext.popIgnoreTextureReplacement();
        }
        return original.call(source, swapped);
    }
}
