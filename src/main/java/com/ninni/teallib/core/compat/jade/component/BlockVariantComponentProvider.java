package com.ninni.teallib.core.compat.jade.component;

import com.ninni.teallib.api.common.item.tooltip.TooltipUtils;
import com.ninni.teallib.core.TealLib;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

public class BlockVariantComponentProvider implements IBlockComponentProvider {

    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath(TealLib.MODID,"block_variant");
    }


    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        Level level = blockAccessor.getBlockEntity().getLevel();
        if (level == null) return;
        ItemStack stack = blockAccessor.getBlock().asItem().getDefaultInstance();
        blockAccessor.getBlockEntity().saveToItem(stack, level.registryAccess());

        MutableComponent variant = TooltipUtils.getJsonBlockEntityVariantTooltip(stack, Item.TooltipContext.of(level), TooltipUtils.GRAY_ITALIC);
        if (variant != null) {
            tooltip.add(variant);
        }
    }
}
