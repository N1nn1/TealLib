package com.ninni.teallib.common.entity.catchable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public interface CustomInventoryRendering {
    boolean isRenderedInTooltip();
    void setIsRenderedInTooltip(boolean bl);
    default boolean animateBob() {
        return true;
    }
    default boolean scaleBaby() {
        return true;
    }
    default int getInventorySkyLight() {
        return 15;
    }
    default int getInventoryBlockLight() {
        return 15;
    }
    default boolean isBabyByDefault() {
        return false;
    }
    default void setCustomData(Entity entity, CompoundTag compoundTag) {}
}
