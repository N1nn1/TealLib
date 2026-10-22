package com.ninni.teallib.core.mixin;

import com.ninni.teallib.api.common.item.ComponentStacking;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(method = "isSameItemSameComponents", at = @At("HEAD"), cancellable = true)
    private static void componentstacking$ignoreComponentsForTaggedItems(ItemStack first, ItemStack second, CallbackInfoReturnable<Boolean> cir) {
        if (ComponentStacking.interceptComponentComparison(first, second)) cir.setReturnValue(true);
    }

    @Inject(method = "isStackable", at = @At("HEAD"), cancellable = true)
    private void componentstacking$allowUnstackableItemsToStack(CallbackInfoReturnable<Boolean> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (ComponentStacking.isConfigured(self)) cir.setReturnValue(true);
    }

    @Inject(method = "setCount", at = @At("HEAD"))
    private void componentstacking$advanceConsumedVariant(int newCount, CallbackInfo ci) {
        ComponentStacking.beforeCountChange((ItemStack) (Object) this, newCount);
    }

    @Inject(method = "split", at = @At("HEAD"), cancellable = true)
    private void componentstacking$restoreComponentsWhenSplit(int amount, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack result = ComponentStacking.splitIfTracked((ItemStack) (Object) this, amount);
        if (result != null) cir.setReturnValue(result);
    }

    @Inject(method = "copyWithCount", at = @At("HEAD"), cancellable = true)
    private void componentstacking$copyMatchingVariantRange(int count, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack result = ComponentStacking.copyWithCountIfTracked((ItemStack) (Object) this, count);
        if (result != null) cir.setReturnValue(result);
    }
}
