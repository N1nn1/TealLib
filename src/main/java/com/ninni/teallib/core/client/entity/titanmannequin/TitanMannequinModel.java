package com.ninni.teallib.core.client.entity.titanmannequin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ninni.teallib.core.TealLib;
import com.ninni.teallib.core.common.entity.TitanMannequin;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class TitanMannequinModel<T extends TitanMannequin> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "titan_mannequin"), "main");

    private final ModelPart leg;
    private final ModelPart head;

    public TitanMannequinModel(ModelPart root) {
        this.leg = root.getChild("leg");
        this.head = root.getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition leg = partdefinition.addOrReplaceChild("leg", CubeListBuilder.create().texOffs(72, 0).addBox(-2.5F, -2.0F, -2.5F, 5.0F, 20.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-9.0F, -18.25F, -9.0F, 18.0F, 20.0F, 18.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-0.5F, -24.25F, 0.0F, 3.0F, 6.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 6).addBox(-12.0F, -8.25F, -5.0F, 3.0F, 6.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 6).mirror().addBox(9.0F, -8.25F, -5.0F, 3.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 22.25F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }

    @Override
    public void setupAnim(T t, float v, float v1, float v2, float v3, float v4) {
        this.head.yRot = v3 * ((float)Math.PI / 180F);
        this.head.xRot = v4 * ((float)Math.PI / 180F);

        float rollDelta  = t.getBodyRoll();
        float pitchDelta = t.getBodyPitch();

        float roll  = (float) -Math.atan2(rollDelta,  10);
        float pitch = (float) -Math.atan2(pitchDelta, 10);

        this.head.zRot = Mth.lerp(0.2F, this.head.zRot, roll);
        this.head.xRot = Mth.lerp(0.2F, this.head.xRot, pitch);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int i1, int i2) {

    }

    public ModelPart getLegPart(int index) {
        //use index if dealing with legs of different models
        return leg;
    }

    public ModelPart getBody() {
        return head;
    }
}
