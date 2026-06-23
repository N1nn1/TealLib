package com.ninni.teallib.server.entity;

import com.ninni.teallib.server.data.entityvariant.EntityVariantManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public interface JsonVariantHolder {
    void setVariant(ResourceLocation string);
    ResourceLocation getVariant();
    ResourceLocation getDefaultVariant();

    default void loadOrAssignVariant(Entity mob, CompoundTag tag, String tagKey) {
        if (tag.contains(tagKey, 99)) {
            EntityVariantManager.getNaturallyOccurringVariant(mob);
        } else if (tag.contains(tagKey, 8)) {
            ResourceLocation rl = ResourceLocation.parse(tag.getString(tagKey));
            if (EntityVariantManager.isValidVariantForType(mob.level().registryAccess(), mob.getType(), rl)) {
                this.setVariant(rl);
            } else {
                EntityVariantManager.getNaturallyOccurringVariant(mob);
            }
        } else {
            EntityVariantManager.getNaturallyOccurringVariant(mob);
        }
    }
}

