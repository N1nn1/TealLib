package com.ninni.teallib.api.common.item;

import com.ninni.teallib.api.common.item.tooltip.TooltipUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VariantBlockItem extends BlockItem {

    public VariantBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> list, @NotNull TooltipFlag isAdvanced) {
        super.appendHoverText(stack, context, list, isAdvanced);
        TooltipUtils.addJsonBlockEntityVariantTooltip(stack, context, list);
    }
}
