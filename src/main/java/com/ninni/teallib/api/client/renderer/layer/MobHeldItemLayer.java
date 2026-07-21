package com.ninni.teallib.api.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.ninni.teallib.api.client.model.ItemHoldingModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class MobHeldItemLayer<T extends LivingEntity, M extends HierarchicalModel<T>> extends RenderLayer<T, M> {
    private final ItemInHandRenderer itemInHandRenderer;
    final float scale;

    public MobHeldItemLayer(RenderLayerParent<T, M> renderer, ItemInHandRenderer itemInHandRenderer, float scale) {
        super(renderer);
        this.itemInHandRenderer = itemInHandRenderer;
        this.scale = scale;
    }

    @Override
    public void render(@NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, @NotNull T mob, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        M parentModel = this.getParentModel();
        if (!(parentModel instanceof ItemHoldingModel model)) return;

        for (ItemHoldingModel.ItemHoldingPart part : model.getItemHoldingParts(mob)) {
            renderItemPart(part, poseStack, buffer, packedLight, mob, parentModel);
        }
    }

    @SuppressWarnings("deprecation")
    private void renderItemPart(ItemHoldingModel.ItemHoldingPart part, PoseStack poseStack, MultiBufferSource buffer, int packedLight, T mob, M parentModel) {
        ItemStack stack = mob.getItemBySlot(part.slot());

        if (stack.isEmpty()) return;
        if (!part.itemPart().visible) return;
        if (!(parentModel instanceof ItemHoldingModel model)) return;

        poseStack.pushPose();

        model.translatePart(part, poseStack);
        for (ModelPart modelPart : part.parents()) modelPart.translateAndRotate(poseStack);
        part.itemPart().translateAndRotate(poseStack);
        model.rotatePart(part, poseStack);

        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.translate(0.0F, -0.2F, 0.0F);
        if (!(stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock().defaultBlockState().isSolid())) {
            if (stack.getItem() instanceof TieredItem) {
                poseStack.mulPose(Axis.YN.rotationDegrees(135.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
                poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
            } else {
                poseStack.translate(0.0F, 0.2F, -0.15F);
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            }
        }

        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

        poseStack.scale(scale, scale, scale);

        this.itemInHandRenderer.renderItem(mob, stack, ItemDisplayContext.GROUND, false, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}

