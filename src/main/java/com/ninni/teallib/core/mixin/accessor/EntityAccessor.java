package com.ninni.teallib.core.mixin.accessor;

import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@OnlyIn(Dist.CLIENT)
@Mixin(Entity.class)
public interface EntityAccessor {

    @Accessor("wasTouchingWater")
    void setTouchingWater(boolean touchingWater);
}
