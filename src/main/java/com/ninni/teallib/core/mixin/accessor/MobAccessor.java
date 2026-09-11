package com.ninni.teallib.core.mixin.accessor;

import org.spongepowered.asm.mixin.gen.Invoker;

@org.spongepowered.asm.mixin.Mixin(net.minecraft.world.entity.Mob.class)
public interface MobAccessor {
    @Invoker
    float callRotlerp(float angle, float targetAngle, float maxIncrease);
}
