package com.ninni.teallib.api.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public interface ItemHoldingModel {

    List<ItemHoldingPart> getItemHoldingParts(LivingEntity entity);

    default void translatePart(ItemHoldingPart part, PoseStack poseStack) {
    }

    default void rotatePart(ItemHoldingPart part, PoseStack poseStack) {
    }

    record ItemHoldingPart(ModelPart itemPart, List<ModelPart> parents, EquipmentSlot slot) {}
}
