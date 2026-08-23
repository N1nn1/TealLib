package com.ninni.teallib.core.mixin;

import com.ninni.teallib.api.common.entity.catchable.CustomInventoryRendering;
import net.minecraft.world.entity.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
public class EntityMixin implements CustomInventoryRendering {
    @Unique private boolean spawn$isRenderedInTooltip;

    @Override
    public boolean renderedInTooltip() {
        return spawn$isRenderedInTooltip;
    }

    @Override
    public void setRenderedInTooltip(boolean bl) {
        spawn$isRenderedInTooltip = bl;
    }
}
