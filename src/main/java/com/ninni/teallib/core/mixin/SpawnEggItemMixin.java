package com.ninni.teallib.core.mixin;

import com.ninni.teallib.api.common.data.variant.util.VariantAttachments;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(SpawnEggItem.class)
public class SpawnEggItemMixin {

    @Inject(method = "spawnOffspringFromSpawnEgg", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;setBaby(Z)V", shift = At.Shift.AFTER))
    private void S$spawnOffspringFromSpawnEgg(Player player, Mob adult, EntityType<? extends Mob> entityType, ServerLevel level, Vec3 pos, ItemStack stack, CallbackInfoReturnable<Optional<Mob>> cir, @Local(ordinal = 1) Mob baby) {
        if (VariantAttachments.has(adult)) VariantAttachments.set(baby, VariantAttachments.get(adult));
    }
}
