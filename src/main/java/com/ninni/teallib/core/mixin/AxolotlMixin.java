package com.ninni.teallib.core.mixin;

import com.ninni.teallib.api.common.entity.catchable.CustomInventoryRendering;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LerpingModel;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;

@Mixin(Axolotl.class)
public abstract class AxolotlMixin extends Animal implements LerpingModel, VariantHolder<Axolotl.Variant>, Bucketable, CustomInventoryRendering {
    @Unique private boolean spawn$isRenderedInTooltip;

    protected AxolotlMixin(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean renderedInTooltip() {
        return spawn$isRenderedInTooltip;
    }

    @Override
    public void setRenderedInTooltip(boolean bl) {
        spawn$isRenderedInTooltip = bl;
    }
}
