package com.ninni.teallib.client.entity;

import com.ninni.teallib.TealLib;
import com.ninni.teallib.common.entity.Mannequin;
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
        return ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "textures/entity/mannequin.png");
    }
}
