package com.ninni.teallib.core.mixin;

import com.ninni.teallib.api.common.item.ComponentStacking;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Inject(method = "merge(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"))
    private static void componentstacking$beginEntityMerge(ItemEntity destinationEntity, ItemStack destinationStack, ItemEntity originEntity, ItemStack originStack, CallbackInfo ci) {
        ComponentStacking.beginEntityMerge(destinationEntity, destinationStack, originEntity, originStack);
    }

    @Inject(method = "merge(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/world/item/ItemStack;)V", at = @At("RETURN"))
    private static void componentstacking$endEntityMerge(ItemEntity destinationEntity, ItemStack destinationStack, ItemEntity originEntity, ItemStack originStack, CallbackInfo ci) {
        ComponentStacking.endEntityMerge(destinationEntity, originEntity);
    }
}
