package com.ninni.teallib.compat.geckolib.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ninni.teallib.api.client.renderer.variant.VariantRenderContext;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

/** GeoRenderLayer is not a vanilla RenderLayer, so without this a GeckoLib overlay gets the body skin. */
@Mixin(GeoRenderer.class)
public interface GeoRendererMixin<T extends software.bernie.geckolib.animatable.GeoAnimatable> {

    @WrapOperation(
            method = "applyRenderLayers",
            at = @At(value = "INVOKE", target = "Lsoftware/bernie/geckolib/renderer/layer/GeoRenderLayer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lsoftware/bernie/geckolib/animatable/GeoAnimatable;Lsoftware/bernie/geckolib/cache/object/BakedGeoModel;Lnet/minecraft/client/renderer/RenderType;Lnet/minecraft/client/renderer/MultiBufferSource;Lcom/mojang/blaze3d/vertex/VertexConsumer;FII)V"),
            remap = false
    )
    private void teallib$scopeGeoLayer(GeoRenderLayer<T> layer, PoseStack poseStack, T animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay, Operation<Void> original) {
        VariantRenderContext.pushScope(VariantRenderContext.Scope.LAYER);
        try {
            original.call(layer, poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
        } finally {
            VariantRenderContext.popScope();
        }
    }

    @WrapOperation(
            method = "applyRenderLayersForBone",
            at = @At(value = "INVOKE", target = "Lsoftware/bernie/geckolib/renderer/layer/GeoRenderLayer;renderForBone(Lcom/mojang/blaze3d/vertex/PoseStack;Lsoftware/bernie/geckolib/animatable/GeoAnimatable;Lsoftware/bernie/geckolib/cache/object/GeoBone;Lnet/minecraft/client/renderer/RenderType;Lnet/minecraft/client/renderer/MultiBufferSource;Lcom/mojang/blaze3d/vertex/VertexConsumer;FII)V"),
            remap = false
    )
    private void teallib$scopeGeoBoneLayer(GeoRenderLayer<T> layer, PoseStack poseStack, T animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay, Operation<Void> original) {
        VariantRenderContext.pushScope(VariantRenderContext.Scope.LAYER);
        try {
            original.call(layer, poseStack, animatable, bone, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
        } finally {
            VariantRenderContext.popScope();
        }
    }
}
