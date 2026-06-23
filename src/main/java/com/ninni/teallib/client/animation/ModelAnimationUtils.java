package com.ninni.teallib.client.animation;

import com.ninni.teallib.common.entity.animation.EntityAnimationController;
import com.ninni.teallib.mixin.HierarchicalModelAccessor;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;
import org.joml.Vector3f;

public class ModelAnimationUtils {
    public static final Vector3f ANIMATION_VECTOR_CACHE = HierarchicalModelAccessor.getANIMATION_VECTOR_CACHE();

    public static void animate(HierarchicalModel<?> model, AnimationState animationState, AnimationDefinition animationDefinition, float ageInTicks, float speed, float degree) {
        animationState.updateTime(ageInTicks, speed);
        animationState.ifStarted(state -> KeyframeAnimations.animate(model, animationDefinition, state.getAccumulatedTime(), degree, ANIMATION_VECTOR_CACHE));
    }

    public static void animate(HierarchicalModel<?> model, AnimationState animationState, AnimationDefinition animationDefinition, float ageInTicks, float degree) {
        animate(model, animationState, animationDefinition, ageInTicks, 1.0F, degree);
    }

    public static void animateProgress(HierarchicalModel<?> model, AnimationDefinition animationDefinition, float progress, float degree) {
        float clamped = Mth.clamp(progress, 0.0F, 1.0F);

        long timeMillis = (long) (clamped * animationDefinition.lengthInSeconds() * 1000.0F);

        KeyframeAnimations.animate(model, animationDefinition, timeMillis, degree, ANIMATION_VECTOR_CACHE);
    }

    public static void animateOppositeProgress(HierarchicalModel<?> model, AnimationDefinition animationDefinition, float progress, float degree) {
        animateProgress(model, animationDefinition, 1.0F - Mth.clamp(progress, 0.0F, 1.0F), degree);
    }

    public static void animate(HierarchicalModel<?> model, EntityAnimationController controller, ResourceLocation id, AnimationDefinition animationDefinition, float ageInTicks, float speed, float degree) {
        animate(model, controller.state(id), animationDefinition, ageInTicks, speed, degree);
    }

    public static void animateProgress(HierarchicalModel<?> model, EntityAnimationController controller, ResourceLocation id, AnimationDefinition animationDefinition, float degree) {
        animateProgress(model, animationDefinition, controller.progress(id), degree);
    }
}