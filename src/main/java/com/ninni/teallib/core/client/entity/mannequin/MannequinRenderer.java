package com.ninni.teallib.core.client.entity.mannequin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ninni.teallib.core.TealLib;
import com.ninni.teallib.core.common.entity.Mannequin;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class MannequinRenderer extends LivingEntityRenderer<Mannequin, MannequinModel> {

    public MannequinRenderer(EntityRendererProvider.Context context) {
        super(context, new MannequinModel(context.bakeLayer(MannequinModel.LAYER_LOCATION)), 0.4f);
    }

    @Override
    protected boolean shouldShowName(@NotNull Mannequin entity) {
        double d0 = this.entityRenderDispatcher.distanceToSqr(entity);
        float f = entity.isCrouching() ? 32.0F : 64.0F;
        return !(d0 >= (double) (f * f)) && entity.isCustomNameVisible();
    }

    @Override
    public ResourceLocation getTextureLocation(Mannequin entity) {
        return ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "textures/entity/mannequin/mannequin_teal.png");
    }

    @Override
    public void render(Mannequin entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

    }
}
