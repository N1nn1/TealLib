package com.ninni.teallib.core.client.entity.titanmannequin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.ninni.teallib.api.common.entity.legged.Leg;
import com.ninni.teallib.core.TealLib;
import com.ninni.teallib.core.common.entity.TitanMannequin;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class TitanMannequinRenderer extends MobRenderer<TitanMannequin, TitanMannequinModel<TitanMannequin>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "textures/entity/titan_mannequin.png");

    public TitanMannequinRenderer(EntityRendererProvider.Context context) {
        super(context, new TitanMannequinModel(context.bakeLayer(TitanMannequinModel.LAYER_LOCATION)), 1F);
    }

    @Override
    public ResourceLocation getTextureLocation(TitanMannequin titanMannequin) {
        return TEXTURE;
    }

    @Override
    public void render(TitanMannequin pEntity, float pEntityYaw, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight) {
        Vec3 entityPos = pEntity.position();
        float bodyRot = pEntity.yBodyRot;
        super.render(pEntity, pEntityYaw, pPartialTick, pPoseStack, pBufferSource, pPackedLight);
        renderBody(pEntity, pPoseStack, pBufferSource, pPartialTick, pPackedLight, this.model.getBody());

        for (int i = 0; i < pEntity.getLegs().length; i++) {
            Leg leg = pEntity.getLegs()[i];
            if (leg == null) continue;

            renderLeg(pPoseStack, pBufferSource, pEntity, leg, pPartialTick, pPackedLight, this.model.getLegPart(i), i);
            if(entityRenderDispatcher.shouldRenderHitBoxes())
                renderDebugMarkers(pPoseStack, pBufferSource, entityPos, leg);
        }
    }

    private void renderBody(TitanMannequin pEntity, PoseStack poseStack, MultiBufferSource pBufferSource, float partialTicks, int packedLight, ModelPart part) {
        double entityX = Mth.lerp(partialTicks, pEntity.getBodyOffseto().x, pEntity.getBodyOffset().x);
        double entityY = Math.max(0, Mth.lerp(partialTicks, pEntity.getBodyOffseto().y, pEntity.getBodyOffset().y));
        double entityZ = Mth.lerp(partialTicks, pEntity.getBodyOffseto().z, pEntity.getBodyOffset().z);

        poseStack.pushPose();

        poseStack.pushPose();

        poseStack.translate(0,1.5,0);
        poseStack.translate(entityX, entityY, entityZ);

        poseStack.mulPose(Axis.XP.rotationDegrees(180));
        poseStack.mulPose(Axis.YP.rotationDegrees(pEntity.getPreciseBodyRotation(partialTicks)));

        //replace with correct overlays
        part.render(poseStack, pBufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        poseStack.popPose();

        poseStack.popPose();
    }

    private void renderLeg(PoseStack poseStack, MultiBufferSource pBufferSource, LivingEntity pEntity, Leg leg, float partialTicks, int packedLight, ModelPart part, int index) {
        Vec3 current  = leg.getCurrentPos();
        Vec3 previous = leg.getPrevPos() != null ? leg.getPrevPos() : current;

        double x = Mth.lerp(partialTicks, previous.x, current.x);
        double y = Mth.lerp(partialTicks, previous.y, current.y);
        double z = Mth.lerp(partialTicks, previous.z, current.z);

        double entityX = Mth.lerp(partialTicks, pEntity.xo, pEntity.getX());
        double entityY = Mth.lerp(partialTicks, pEntity.yo, pEntity.getY());
        double entityZ = Mth.lerp(partialTicks, pEntity.zo, pEntity.getZ());

        poseStack.pushPose();

        poseStack.pushPose();
        poseStack.translate(0,1.5,0);
        poseStack.translate(x - entityX, y - entityY, z - entityZ);
        poseStack.mulPose(Axis.XP.rotationDegrees(180));
        poseStack.mulPose(Axis.YP.rotationDegrees(pEntity.getPreciseBodyRotation(partialTicks)));

        //replace with correct overlays
        part.render(poseStack, pBufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        poseStack.popPose();

        poseStack.popPose();
    }

    private void renderDebugMarkers(PoseStack pPoseStack, MultiBufferSource pBufferSource, Vec3 entityPos, Leg leg) {
        renderDebugBox(pPoseStack, pBufferSource, leg.getTargetPos(), entityPos, 1.0f, 0.0f, 0.0f, 1.0f);
        renderDebugBox(pPoseStack, pBufferSource, leg.getCurrentPos(), entityPos, 0.0f, 0.0f, 1.0f, 1.0f);
    }

    private void renderDebugBox(PoseStack poseStack, MultiBufferSource buffer, Vec3 worldPos, Vec3 entityPos, float r, float g, float b, float a) {
        poseStack.pushPose();
        poseStack.translate(worldPos.x - entityPos.x, worldPos.y - entityPos.y, worldPos.z - entityPos.z);
        LevelRenderer.renderLineBox(poseStack, buffer.getBuffer(RenderType.lines()), new AABB(-0.1, -0.1, -0.1, 0.1, 0.1, 0.1), r, g, b, a);
        poseStack.popPose();
    }
}
