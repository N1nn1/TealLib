package com.ninni.teallib.core.mixin.client;

import com.ninni.teallib.api.common.entity.catchable.CustomInventoryRendering;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.AxolotlModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LerpingModel;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AxolotlModel.class)
public abstract class AxolotlModelMixin<T extends Axolotl & LerpingModel> extends AgeableListModel<T> {

    @Shadow @Final private ModelPart body;
    @Shadow @Final private ModelPart head;
    @Shadow @Final private ModelPart leftHindLeg;
    @Shadow @Final private ModelPart rightHindLeg;
    @Shadow @Final private ModelPart leftFrontLeg;
    @Shadow @Final private ModelPart rightFrontLeg;
    @Shadow @Final private ModelPart leftGills;
    @Shadow @Final private ModelPart rightGills;
    @Shadow @Final private ModelPart topGills;
    @Shadow @Final private ModelPart tail;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/animal/axolotl/Axolotl;FFFFF)V", at = @At("HEAD"), cancellable = true)
    private void setupAnim(T axolotl, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch, CallbackInfo ci) {
        if (axolotl instanceof CustomInventoryRendering pose && pose.isRenderedInTooltip()) {
            ci.cancel();
            this.body.setRotation(0.15f, 0, 0.0F);
            this.head.setRotation(-0.25f, 0.0F, 0.0F);
            this.leftHindLeg.setRotation(0.65f, 0.0F, 0.0F);
            this.rightHindLeg.setRotation(0.625f, 0.0F, 0.0F);
            this.leftFrontLeg.setRotation(0.5f, 0.0F, 0.0F);
            this.rightFrontLeg.setRotation(0.475f, 0.0F, 0.0F);
            this.leftGills.setRotation(0.0F, 0.25F, 0.0F);
            this.rightGills.setRotation(0.0F, -0.25F, 0.0F);
            this.topGills.setRotation(-0.25F, 0.0F, 0.0F);
            this.tail.setRotation(0.15f, 1.0F, 0.0F);
        }
    }
}
