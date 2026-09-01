package com.ninni.teallib.core.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ninni.teallib.api.client.renderer.variant.VariantRenderContext;
import com.ninni.teallib.api.common.data.variant.VariantTarget;
import com.ninni.teallib.api.common.data.variant.VariantTextureSlot;
import com.ninni.teallib.api.common.data.variant.VariantTextureSlots;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderLayer.class)
public abstract class RenderLayerMixin<T extends Entity, M extends EntityModel<T>> {

    @Inject(method = "renderColoredCutoutModel", at = @At(value = "HEAD"))
    private static <T extends LivingEntity> void teallib$pushEntity(EntityModel<T> model, ResourceLocation resourceLocation, PoseStack p_117379_, MultiBufferSource p_117380_, int p_117381_, T entity, int p_350384_, CallbackInfo ci) {
        VariantRenderContext.push(entity);
    }
    @Inject(method = "renderColoredCutoutModel", at = @At(value = "TAIL"))
    private static <T extends LivingEntity> void teallib$popEntity(EntityModel<T> model, ResourceLocation resourceLocation, PoseStack p_117379_, MultiBufferSource p_117380_, int p_117381_, T entity, int p_350384_, CallbackInfo ci) {
        VariantRenderContext.pop();
    }
}
