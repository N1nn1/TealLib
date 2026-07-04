package com.ninni.teallib.api.common.entity.catchable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

/**
 * Allows a captured entity to customize how it is rendered
 * inside inventories and tooltips.
 * <p>
 * Implementations can control lighting, scaling, animation and
 * additional rendering data.
 */
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
