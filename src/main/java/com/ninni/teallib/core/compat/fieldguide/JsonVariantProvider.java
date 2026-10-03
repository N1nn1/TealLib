package com.ninni.teallib.core.compat.fieldguide;

import com.evandev.fieldguide.api.variant.VariantDef;
import com.evandev.fieldguide.api.variant.VariantProvider;
import com.ninni.teallib.api.common.data.variant.VariantDefinition;
import com.ninni.teallib.api.common.data.variant.VariantManager;
import com.ninni.teallib.api.common.data.variant.VariantTarget;
import com.ninni.teallib.api.common.data.variant.util.VariantAttachments;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

import java.util.ArrayList;
import java.util.List;

public class JsonVariantProvider<T extends Mob> implements VariantProvider<T> {
    private static final VariantDef NONE = new VariantDef("default", null);

    @Override
    public List<VariantDef> getVariants(T entity) {
        List<VariantDef> variants = new ArrayList<>();
        if (!hasTealVariants(entity)) return variants;

        VariantTarget target = VariantTarget.of(entity.getType());
        for (VariantDefinition definition : VariantManager.getAllVariantsFor(entity.registryAccess(), target, false)) {
            if (definition != null && definition.supports(target)) {
                variants.add(toDef(entity, definition.id()));
            }
        }

        return variants;
    }

    @Override
    public boolean isDefaultVariant(T entity, VariantDef def) {
        return VariantManager.getDefaultVariantId().equals(def.value());
    }

    @Override
    public void apply(T entity, VariantDef def) {
        if (def.value() instanceof ResourceLocation id) {
            VariantAttachments.set(entity, id);
        } else if (hasTealVariants(entity)) {
            VariantAttachments.set(entity, VariantManager.getDefaultVariantId());
        }
    }

    @Override
    public VariantDef getCurrent(T entity) {
        if (!hasTealVariants(entity)) return NONE;
        return toDef(entity, VariantAttachments.has(entity) ? VariantAttachments.get(entity) : VariantManager.getDefaultVariantId());
    }

    @Override
    public String getCacheKey(T entity) {
        return "tealvariant_" + BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
    }

    private static boolean hasTealVariants(Mob entity) {
        return VariantManager.getVariantCountFor(entity.registryAccess(), VariantTarget.of(entity.getType()), false) > 1;
    }

    private static VariantDef toDef(Mob entity, ResourceLocation variantId) {
        String id = "tealvariant." + BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()) + "." + variantId;
        return new VariantDef(id.replace(":", "."), variantId);
    }
}
