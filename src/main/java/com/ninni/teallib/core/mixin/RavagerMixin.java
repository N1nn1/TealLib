package com.ninni.teallib.core.mixin;

import com.ninni.teallib.api.common.entity.EntityUtils;
import com.ninni.teallib.core.TealLib;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Ravager.class)
public abstract class RavagerMixin extends Raider {

    protected RavagerMixin(EntityType<? extends Raider> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "stunEffect", at = @At(value = "TAIL"))
    public void S$use(CallbackInfo ci) {
        if (TealLib.COMMON_CONFIG.vanillaMobsSpawnStunParticles.get()) EntityUtils.spawnStunParticles(this, 1, 0.8f, 0.4f);
    }
}
