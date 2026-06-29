package com.ninni.teallib.compat.fieldguide;

import com.evandev.fieldguide.api.variant.VariantDef;
import com.evandev.fieldguide.api.variant.VariantProvider;
import com.ninni.teallib.common.data.entityvariant.EntityVariantManager;
import com.ninni.teallib.common.entity.variant.JsonVariantHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

import java.util.ArrayList;
import java.util.List;

public class JsonVariantProvider<T extends Mob & JsonVariantHolder> implements VariantProvider<T> {

    @Override
    public List<VariantDef> getVariants(T entity) {
        List<VariantDef> variants = new ArrayList<>();
        ResourceLocation defaultVariant = entity.getDefaultVariant();

        if (EntityVariantManager.getVariantCountFor(entity.registryAccess(), entity.getType()) <= 1) return variants;

        if (defaultVariant != null) {
            variants.add(new VariantDef(defaultVariant.toString(), defaultVariant));
        }

        for (EntityVariantManager.EntityVariantData data : EntityVariantManager.all(entity.registryAccess())) {
            if (data == null || data.hidden()) continue;

            if (data.type().equals(entity.getType()) || (data.childType().isPresent() && data.childType().get().equals(entity.getType()))) {
                if (!data.id().equals(defaultVariant)) {
                    variants.add(new VariantDef(data.id().toString(), data.id()));
                }
            }
        }

        return variants;
    }

    @Override
    public void apply(T entity, VariantDef def) {
        if (def.value() instanceof ResourceLocation id) entity.setVariant(id);
    }

    @Override
    public VariantDef getCurrent(T entity) {
        ResourceLocation current = entity.getVariant();
        return new VariantDef(current.toString(), current);
    }
}
