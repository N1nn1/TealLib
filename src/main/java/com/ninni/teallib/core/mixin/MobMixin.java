package com.ninni.teallib.core.mixin;

import com.ninni.teallib.api.common.entity.CustomDamageType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.extensions.ILivingEntityExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Mob.class)
public abstract class MobMixin extends Entity implements Attackable, ILivingEntityExtension {

    public MobMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Redirect(method = "doHurtTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/DamageSources;mobAttack(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/damagesource/DamageSource;"))
    public DamageSource S$travel(DamageSources instance, LivingEntity living) {
        if (living instanceof CustomDamageType type) {
            return instance.source(type.getDamageType(), living);
        } else {
            return instance.mobAttack(living);
        }
    }
}
