package com.ninni.teallib.core.mixin;

import com.ninni.teallib.core.registry.TealTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Biome.class)
public abstract class BiomeMixin {

    @Inject(method = "shouldFreeze(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Z)Z", at = @At("HEAD"), cancellable = true)
    private void s$shouldFreeze(LevelReader level, BlockPos water, boolean mustBeAtEdge, CallbackInfoReturnable<Boolean> cir) {
        if (level.getBiome(water).is(TealTags.DOESNT_FREEZE)) {
            cir.setReturnValue(false);
        }
    }
    @Inject(method = "shouldSnow", at = @At("HEAD"), cancellable = true)
    private void s$shouldSnow(LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (level.getBiome(pos).is(TealTags.DOESNT_SNOW)) {
            cir.setReturnValue(false);
        }
    }
}
