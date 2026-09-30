package com.ninni.teallib.core.compat.jade.component;

import com.ninni.teallib.api.common.data.variant.EntityVariantComponents;
import com.ninni.teallib.api.common.data.variant.util.VariantAttachments;
import com.ninni.teallib.core.TealLib;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.List;
import java.util.Optional;

public class EntityVariantComponentProvider implements IEntityComponentProvider {

    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath(TealLib.MODID,"entity_variant");
    }

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        if (!(accessor.getEntity() instanceof LivingEntity living)) return;

        EntityType<?> type = living.getType();
        ResourceLocation location = BuiltInRegistries.ENTITY_TYPE.getKey(type);

        CompoundTag tag = new CompoundTag();
        living.save(tag);
        VariantAttachments.toTag(living, tag);

        Optional<List<Component>> rows = EntityVariantComponents.getComponents(tag, Item.TooltipContext.of(living.level()), location);
        if (rows.isEmpty() || rows.get().isEmpty()) return;
        tooltip.addAll(rows.get());
    }
}
