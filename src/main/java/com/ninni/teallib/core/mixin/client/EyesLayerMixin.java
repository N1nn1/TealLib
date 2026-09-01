package com.ninni.teallib.core.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ninni.teallib.api.client.renderer.variant.VariantRenderContext;
import com.ninni.teallib.api.client.renderer.variant.VariantRenderManager;
import com.ninni.teallib.api.common.data.variant.VariantTextureSlot;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EyesLayer.class)
public abstract class EyesLayerMixin<T extends Entity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    public EyesLayerMixin(RenderLayerParent<T, M> p_117346_) {
        super(p_117346_);
    }


    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/EyesLayer;renderType()Lnet/minecraft/client/renderer/RenderType;"))
    private RenderType teallib$variantEyes(EyesLayer<T, M> layer, Operation<RenderType> original) {
        VariantTextureSlot slot = VariantRenderContext.getSlot();
        if (slot == null) return original.call(layer);
        if (!(VariantRenderContext.get() instanceof LivingEntity entity)) return original.call(layer);

        ResourceLocation originalTexture = VariantRenderManager.getTextureForSlot(entity, null, slot);

        if (originalTexture == null) return original.call(layer);
        return RenderType.eyes(originalTexture);
    }
}
