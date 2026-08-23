package com.ninni.teallib.core.mixin.client;

import com.ninni.teallib.api.common.entity.catchable.CustomInventoryRendering;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements RenderLayerParent<T, M> {

    protected LivingEntityRendererMixin(EntityRendererProvider.Context p_174008_) {
        super(p_174008_);
    }

    @Inject(method = "getBob", at = @At("HEAD"), cancellable = true)
    private void spawn$mobInteract(T e, float p_115306_, CallbackInfoReturnable<Float> cir) {
        if (e instanceof CustomInventoryRendering custom && custom.renderedInTooltip()) {
            cir.setReturnValue(0f);
        }
    }
}
