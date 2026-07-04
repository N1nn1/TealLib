package com.ninni.teallib.core.client.entity;

import com.ninni.teallib.core.TealLib;
import com.ninni.teallib.api.client.animation.ModelAnimationUtils;
import com.ninni.teallib.core.common.entity.Mannequin;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class MannequinModel extends HierarchicalModel<Mannequin> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "mannequin"), "main");

    private final ModelPart root;
    private final ModelPart all;
    private final ModelPart rod;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart head;

    public MannequinModel(ModelPart root) {
        this.root = root;
        this.all = root.getChild("all");
        this.rod = this.all.getChild("rod");
        this.body = this.rod.getChild("body");
        this.rightArm = this.body.getChild("rightArm");
        this.leftArm = this.body.getChild("leftArm");
        this.head = this.body.getChild("head");
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(Mannequin entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root.getAllParts().forEach(ModelPart::resetPose);
        this.root.yRot = netHeadYaw * ((float) Math.PI / 180F);

        float partial = ageInTicks - entity.tickCount;
        float smoothRotation = Mth.lerp(partial, entity.prevRotation, entity.getRotation());

        ModelAnimationUtils.animate(this, entity.animations(), Mannequin.PUNCH, MannequinAnimations.PUNCH, ageInTicks, 1.0f, 1.0F);
        ModelAnimationUtils.animate(this, entity.animations(), Mannequin.SIT, MannequinAnimations.SIT, ageInTicks, 1.0f, 1.0F);

        if (entity.animations().isStopped(Mannequin.SIT) && entity.animations().isStopped(Mannequin.STAND_UP)) {
            ModelAnimationUtils.animate(this, entity.animations(), Mannequin.SITTING, MannequinAnimations.SITTING, ageInTicks, 1.0F, 1.0F);
        }

        ModelAnimationUtils.animate(this, entity.animations(), Mannequin.STAND_UP, MannequinAnimations.STAND_UP, ageInTicks, 1.0f, 1.0F);
        ModelAnimationUtils.animateProgress(this, entity.animations(), Mannequin.ROTATION, MannequinAnimations.ROTATION, smoothRotation);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition all = partdefinition.addOrReplaceChild("all",
                CubeListBuilder.create().texOffs(0, 48).addBox(-5.0F, -1.0F, -5.0F, 10.0F, 1.0F, 10.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition rod = all.addOrReplaceChild("rod",
                CubeListBuilder.create().texOffs(0, 23).addBox(-2.0F, -11.0F, -2.0F, 4.0F, 11.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -1.0F, 0.0F));

        PartDefinition body = rod.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, -12.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(16, 32).addBox(-4.0F, -12.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
                PartPose.offset(0.0F, -11.0F, 0.0F));

        PartDefinition rightArm = body.addOrReplaceChild("rightArm",
                CubeListBuilder.create().texOffs(40, 16).addBox(-4.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-4.0F, -10.0F, 0.0F));

        PartDefinition leftArm = body.addOrReplaceChild("leftArm",
                CubeListBuilder.create().texOffs(48, 32).addBox(0.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(4.0F, -10.0F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                        .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)),
                PartPose.offset(0.0F, -12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }
}