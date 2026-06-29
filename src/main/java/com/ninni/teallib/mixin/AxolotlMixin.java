package com.ninni.teallib.mixin;

import com.ninni.teallib.common.entity.catchable.CustomInventoryRendering;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LerpingModel;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Axolotl.class)
public abstract class AxolotlMixin extends Animal implements LerpingModel, VariantHolder<Axolotl.Variant>, Bucketable, CustomInventoryRendering {
    @Unique private boolean spawn$isRenderedInTooltip;

    protected AxolotlMixin(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean isRenderedInTooltip() {
        return spawn$isRenderedInTooltip;
    }

    @Override
    public void setIsRenderedInTooltip(boolean bl) {
        spawn$isRenderedInTooltip = bl;
    }
}
