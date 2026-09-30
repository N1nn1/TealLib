package com.ninni.teallib.core.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ninni.teallib.api.client.renderer.RenderUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class ItemRenderMixin {

    @Inject(method = "renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V", at = @At("HEAD"))
    private void spawn$setItemRenderEntity(LivingEntity p_270101_, ItemStack p_270637_, ItemDisplayContext p_270437_, boolean p_270434_, PoseStack p_270230_, MultiBufferSource p_270411_, Level p_270641_, int p_270595_, int p_270927_, int p_270845_, CallbackInfo ci) {
        RenderUtils.ItemRenderContext.set(p_270101_);
    }
    @Inject(method = "renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V", at = @At("RETURN"))
    private void spawn$clearItemRenderEntity(LivingEntity entity, ItemStack stack, ItemDisplayContext context, boolean leftHand, PoseStack poseStack, MultiBufferSource buffer, Level level, int light, int overlay, int seed, CallbackInfo ci) {
        RenderUtils.ItemRenderContext.clear();
    }
}
