package com.ninni.teallib.core.compat.fieldguide;

import com.evandev.fieldguide.api.variant.VariantDef;
import com.evandev.fieldguide.api.variant.VariantProvider;
import com.ninni.teallib.api.common.data.variant.VariantDefinition;
import com.ninni.teallib.api.common.data.variant.VariantManager;
import com.ninni.teallib.api.common.data.variant.VariantTarget;
import com.ninni.teallib.api.common.data.variant.util.VariantAttachments;
import com.ninni.teallib.core.TealLib;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

import java.util.ArrayList;
import java.util.List;

public class JsonVariantProvider<T extends Mob> implements VariantProvider<T> {

    @Override
    public List<VariantDef> getVariants(T entity) {
        List<VariantDef> variants = new ArrayList<>();

        VariantTarget target = VariantTarget.of(entity.getType());
        List<VariantDefinition> definitions = VariantManager.getAllVariantsFor(entity.registryAccess(), target, false);

        if (!definitions.isEmpty()) {
            for (VariantDefinition definition : definitions) {
                if (definition != null && definition.supports(target)) {
                    if (definition == VariantManager.getDefaultVariant(target) && VariantManager.getVariantCountFor(entity.registryAccess(), target, false) <= 1) continue;
                    String id = "tealvariant." + BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()) + "." + definition.id().toString();
                    variants.add(new VariantDef(id.replace(":", "."), definition.id()));
                }
            }
        }

        return variants;
    }

    @Override
    public boolean suppressesDefaultVariant(T entity) {
        return true;
    }

    @Override
    public void apply(T entity, VariantDef def) {
        if (def.value() instanceof ResourceLocation id) {
            VariantAttachments.set(entity, id);
        }
    }

    @Override
    public VariantDef getCurrent(T entity) {
        ResourceLocation variantId = VariantAttachments.has(entity) ? VariantAttachments.get(entity) : VariantManager.getDefaultVariantId();
        String id = "tealvariant." + BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()) + "." + variantId;
        return new VariantDef(id.replace(":", "."), variantId);
    }
}
