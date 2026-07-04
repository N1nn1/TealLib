package com.ninni.teallib.api.common.entity.variant;

import com.ninni.teallib.api.common.data.entityvariant.EntityVariantManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/**
 * Represents an Entity whose variant is defined by
 * {@link EntityVariantManager Entity Variant Data}.
 * <p>
 * Implementations store a Variant ID and provide a default variant
 * used whenever no valid saved variant can be restored.
 */
public interface JsonVariantHolder {

    void setVariant(ResourceLocation string);
    ResourceLocation getVariant();
    ResourceLocation getDefaultVariant();

    /**
     * Loads a saved Variant ID from NBT or assigns a naturally
     * occurring variant if none can be restored.
     * <p>
     * Invalid or missing variants automatically fall back to a
     * naturally selected variant.
     *
     * @param mob The Entity.
     * @param tag The Compound Tag containing the saved data.
     * @param tagKey The NBT key storing the Variant ID.
     */
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

