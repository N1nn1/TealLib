package com.ninni.teallib.api.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ninni.teallib.api.client.renderer.RenderUtils;
import com.ninni.teallib.api.common.data.variant.VariantDefinition;
import com.ninni.teallib.api.common.data.variant.VariantManager;
import com.ninni.teallib.api.common.data.variant.VariantTarget;
import com.ninni.teallib.core.TealLib;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class VariantItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final IClientItemExtensions EXTENSIONS =
            new IClientItemExtensions() {
                private final BlockEntityWithoutLevelRenderer renderer = new VariantItemRenderer();
                @Override
                public @NotNull BlockEntityWithoutLevelRenderer getCustomRenderer() {
                    return renderer;
                }
            };

    public VariantItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext context, @NotNull PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null) return;

        VariantDefinition variant = null;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        ResourceLocation loc;

        if (stack.has(DataComponents.BLOCK_ENTITY_DATA)) {
            CustomData customData = stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY);
            CompoundTag tag = customData.copyTag();

            ResourceLocation beId = ResourceLocation.parse(tag.getString("id"));
            BlockEntityType<?> type = BuiltInRegistries.BLOCK_ENTITY_TYPE.get(beId);
            VariantTarget target = VariantTarget.of(type);


            Optional<VariantDefinition> nameTagOverride = VariantManager.getNameTagOverride(mc.level.registryAccess(), target, stack.getHoverName());

            if (nameTagOverride.isPresent()) variant = nameTagOverride.get();

            if ((nameTagOverride.isEmpty() || variant == null) && tag.contains("neoforge:attachments", Tag.TAG_COMPOUND)) {
                CompoundTag neoforgeTag = tag.getCompound("neoforge:attachments");

                if (VariantManager.getVariantCountFor(mc.level.registryAccess(), target, true) > 1) {
                    String string = neoforgeTag.getCompound("teallib:variant").getString("variant");
                    variant = VariantManager.get(mc.level.registryAccess(), target, ResourceLocation.parse(string), false);
                }
            }

            if (variant == null) variant = VariantManager.getDefaultVariant(VariantTarget.of(type));
        }



        if (variant != null) {
            loc = ResourceLocation.fromNamespaceAndPath(variant.id().getNamespace(), "item/tealvariant/" + id.getPath() + "/" + variant.id().getPath());
        } else {
            loc = ResourceLocation.fromNamespaceAndPath(VariantManager.getDefaultVariantId().getNamespace(), "item/tealvariant/" + id.getPath() + "/" + VariantManager.getDefaultVariantId().getPath());
        }

        poseStack.pushPose();

        poseStack.translate(0.5f, 0.5f, 0.5f);
        boolean leftHand = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;

        RenderUtils.ResolvedItemModel.resolve(loc).emit(stack, context, leftHand, poseStack, buffer, light, overlay);
        poseStack.popPose();
    }
}