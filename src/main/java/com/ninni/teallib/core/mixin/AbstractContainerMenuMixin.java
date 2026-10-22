package com.ninni.teallib.core.mixin;

import com.ninni.teallib.api.common.item.ComponentStacking;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {
    @Inject(method = "doClick", at = @At("HEAD"))
    private void componentstacking$beginClickTransaction(int slotIndex, int button, ClickType clickType, Player player, CallbackInfo ci) {
        ComponentStacking.beginMenuTransaction((AbstractContainerMenu) (Object) this);
    }

    @Inject(method = "doClick", at = @At("RETURN"))
    private void componentstacking$endClickTransaction(int slotIndex, int button, ClickType clickType, Player player, CallbackInfo ci) {
        ComponentStacking.endTransaction();
    }
}
