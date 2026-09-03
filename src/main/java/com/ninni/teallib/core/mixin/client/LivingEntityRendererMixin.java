package com.ninni.teallib.core.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ninni.teallib.api.client.renderer.variant.VariantRenderContext;
import com.ninni.teallib.api.common.data.variant.VariantTarget;
import com.ninni.teallib.api.common.data.variant.VariantTextureSlot;
import com.ninni.teallib.api.common.data.variant.VariantTextureSlots;
import com.ninni.teallib.api.common.entity.catchable.CustomInventoryRendering;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(value = LivingEntityRenderer.class, priority = 2000)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements RenderLayerParent<T, M> {

    protected LivingEntityRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @WrapOperation(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;getRenderType(Lnet/minecraft/world/entity/LivingEntity;ZZZ)Lnet/minecraft/client/renderer/RenderType;"), require = 1)
    private RenderType teallib$renderBase(LivingEntityRenderer<T, M> instance, T entity, boolean bodyVisible, boolean translucent, boolean glowing, Operation<RenderType> original) {
        VariantTextureSlot slot = VariantTextureSlots.findBase(entity, VariantTarget.of(entity.getType()));

        VariantRenderContext.pushScope(VariantRenderContext.Scope.BASE);
        if (slot != null) VariantRenderContext.pushSlot(slot);

        try {
            return original.call(instance, entity, bodyVisible, translucent, glowing);
        } finally {
            if (slot != null) VariantRenderContext.popSlot();
            VariantRenderContext.popScope();
        }
    }

    @WrapOperation(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/RenderLayer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/Entity;FFFFFF)V"), require = 1)
    private void teallib$renderLayer(RenderLayer<T, M> layer, PoseStack poseStack, MultiBufferSource buffer, int packedLight, Entity entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch, Operation<Void> original) {
        VariantTextureSlot slot = VariantTextureSlots.find(entity, VariantTarget.of(entity.getType()), layer.getClass());

        VariantRenderContext.pushScope(VariantRenderContext.Scope.LAYER);
        if (slot != null) VariantRenderContext.pushSlot(slot);

        try {
            original.call(layer, poseStack, buffer, packedLight, entity, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch);
        } finally {
            if (slot != null) VariantRenderContext.popSlot();
            VariantRenderContext.popScope();
        }
    }

    @Inject(method = "getBob", at = @At("HEAD"), cancellable = true)
    private void spawn$getBob(T entity, float partialTick, CallbackInfoReturnable<Float> cir) {
        if (entity instanceof CustomInventoryRendering custom && custom.renderedInTooltip()) {
            cir.setReturnValue(0f);
        }
    }
}