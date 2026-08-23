package com.ninni.teallib.core.mixin;

import com.ninni.teallib.api.common.entity.catchable.CustomInventoryRendering;
import net.minecraft.world.entity.animal.WaterAnimal;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(WaterAnimal.class)
public abstract class WaterAnimalMixin implements CustomInventoryRendering {
    @Override
    public boolean animateBob() {
        return true;
    }
}
