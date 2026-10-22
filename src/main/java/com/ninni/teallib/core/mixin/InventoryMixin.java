package com.ninni.teallib.core.mixin;

import com.ninni.teallib.api.common.item.ComponentStacking;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public abstract class InventoryMixin {
    @Inject(method = "add(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"))
    private void componentstacking$beginAddTransaction(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        ComponentStacking.beginInventoryTransaction((Inventory) (Object) this, stack);
    }

    @Inject(method = "add(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("RETURN"))
    private void componentstacking$endAddTransaction(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        ComponentStacking.endTransaction();
    }

    @Inject(method = "add(ILnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"))
    private void componentstacking$beginAddSlotTransaction(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        ComponentStacking.beginInventoryTransaction((Inventory) (Object) this, stack);
    }

    @Inject(method = "add(ILnet/minecraft/world/item/ItemStack;)Z", at = @At("RETURN"))
    private void componentstacking$endAddSlotTransaction(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        ComponentStacking.endTransaction();
    }

    @Inject(method = "placeItemBackInInventory(Lnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"))
    private void componentstacking$beginPlaceBackTransaction(ItemStack stack, CallbackInfo ci) {
        ComponentStacking.beginInventoryTransaction((Inventory) (Object) this, stack);
    }

    @Inject(method = "placeItemBackInInventory(Lnet/minecraft/world/item/ItemStack;)V", at = @At("RETURN"))
    private void componentstacking$endPlaceBackTransaction(ItemStack stack, CallbackInfo ci) {
        ComponentStacking.endTransaction();
    }

    @Inject(method = "placeItemBackInInventory(Lnet/minecraft/world/item/ItemStack;Z)V", at = @At("HEAD"))
    private void componentstacking$beginPlaceBackNotifyingTransaction(ItemStack stack, boolean notifiesClient, CallbackInfo ci) {
        ComponentStacking.beginInventoryTransaction((Inventory) (Object) this, stack);
    }

    @Inject(method = "placeItemBackInInventory(Lnet/minecraft/world/item/ItemStack;Z)V", at = @At("RETURN"))
    private void componentstacking$endPlaceBackNotifyingTransaction(ItemStack stack, boolean notifiesClient, CallbackInfo ci) {
        ComponentStacking.endTransaction();
    }
}
