package com.ninni.teallib.api.common.item;

import com.ninni.teallib.api.common.data.blockvariant.BlockVariantManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
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

        if (context.level() == null) return;

        if (stack.has(DataComponents.BLOCK_ENTITY_DATA)) {
            CompoundTag tag = stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY).copyTag();

            if (tag.contains("Variant", Tag.TAG_STRING)) {
                ResourceLocation beId;

                if (tag.contains("VariantId")) beId = ResourceLocation.tryParse(tag.getString("VariantId"));
                else beId = ResourceLocation.tryParse(tag.getString("id"));
                if (beId != null) {
                    if (BlockVariantManager.getVariantCountFor(context.level().registryAccess(), BuiltInRegistries.BLOCK_ENTITY_TYPE.get(beId)) > 1) {
                        ResourceLocation variant = ResourceLocation.tryParse(tag.getString("Variant"));
                        if (variant != null) {
                            list.add(Component.translatable("variant." + variant.getNamespace() + "." + beId.getPath() + "." + variant.getPath()).withStyle(Style.EMPTY.withItalic(true).withColor(ChatFormatting.GRAY)));
                        }
                    }
                }
            }
        }

    }
}
