package com.ninni.teallib.api.client.animation;

import com.ninni.teallib.api.common.entity.animation.EntityAnimationController;
import com.ninni.teallib.core.mixin.accessor.HierarchicalModelAccessor;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;
import net.neoforged.neoforge.client.entity.animation.json.AnimationHolder;
import org.joml.Vector3f;

/**
 * A utility class that holds a bunch of methods useful for animating using {@link AnimationDefinition AnimationDefinitions}, has integration with {@link EntityAnimationController EntityAnimationController}
 */
public class ModelAnimationUtils {
    public static final Vector3f ANIMATION_VECTOR_CACHE = HierarchicalModelAccessor.getANIMATION_VECTOR_CACHE();

    /**
     * Like the regular {@link HierarchicalModel#animate(AnimationState, AnimationHolder, float) animate} method from {@link HierarchicalModel HierarchicalModel}
     * but capable of changing the {@code degree} of the Animation, which means it can be used for lerping and other things for smoother animations.
     * <p>
     * utilizes {@link EntityAnimationController EntityAnimationController} to obtain the {@link AnimationState AnimationState}
     */
    public static void animate(HierarchicalModel<?> model, EntityAnimationController controller, ResourceLocation id, AnimationDefinition animationDefinition, float ageInTicks, float speed, float degree) {
        animate(model, controller.state(id), animationDefinition, ageInTicks, speed, degree);
    }

    /**
     * Animates an Animation with a given progress. If the progress is {@code 0} the animation will display the first frame, if it's {@code 1} the animation will display the last frame.
     * <p>
     * utilizes {@link EntityAnimationController EntityAnimationController} to obtain the {@link AnimationState AnimationState}
     */
    public static void animateProgress(HierarchicalModel<?> model, EntityAnimationController controller, ResourceLocation id, AnimationDefinition animationDefinition, float degree) {
        animateProgress(model, animationDefinition, controller.progress(id), degree);
    }

    /**
     * Animates an Animation with the opposite of a given progress. If the progress is {@code 0} the animation will display the last frame, if it's {@code 1} the animation will display the first frame.
     * <p>
     * utilizes {@link EntityAnimationController EntityAnimationController} to obtain the {@link AnimationState AnimationState}
     */
    public static void animateOppositeProgress(HierarchicalModel<?> model, EntityAnimationController controller, ResourceLocation id, AnimationDefinition animationDefinition, float degree) {
        animateProgress(model, animationDefinition, 1.0F - Mth.clamp(controller.progress(id), 0.0F, 1.0F), degree);
    }

    /**
     * like {@link ModelAnimationUtils#animate(HierarchicalModel, EntityAnimationController, ResourceLocation, AnimationDefinition, float, float, float) animate} but not utilizing {@link EntityAnimationController EntityAnimationController}
     */
    public static void animate(HierarchicalModel<?> model, AnimationState animationState, AnimationDefinition animationDefinition, float ageInTicks, float speed, float degree) {
        animationState.updateTime(ageInTicks, speed);
        animationState.ifStarted(state -> KeyframeAnimations.animate(model, animationDefinition, state.getAccumulatedTime(), degree, ANIMATION_VECTOR_CACHE));
    }

    public static void animate(HierarchicalModel<?> model, AnimationState animationState, AnimationDefinition animationDefinition, float ageInTicks, float degree) {
        animate(model, animationState, animationDefinition, ageInTicks, 1.0F, degree);
    }

    /**
     * like {@link ModelAnimationUtils#animateProgress(HierarchicalModel, EntityAnimationController, ResourceLocation, AnimationDefinition, float)  animateProgress} but not utilizing {@link EntityAnimationController EntityAnimationController}
     */
    public static void animateProgress(HierarchicalModel<?> model, AnimationDefinition animationDefinition, float progress, float degree) {
        float clamped = Mth.clamp(progress, 0.0F, 1.0F);

        long timeMillis = (long) (clamped * animationDefinition.lengthInSeconds() * 1000.0F);

        KeyframeAnimations.animate(model, animationDefinition, timeMillis, degree, ANIMATION_VECTOR_CACHE);
    }

    /**
     * like {@link ModelAnimationUtils#animateOppositeProgress(HierarchicalModel, EntityAnimationController, ResourceLocation, AnimationDefinition, float)  animateProgress} but not utilizing {@link EntityAnimationController EntityAnimationController}
     */
    public static void animateOppositeProgress(HierarchicalModel<?> model, AnimationDefinition animationDefinition, float progress, float degree) {
        animateProgress(model, animationDefinition, 1.0F - Mth.clamp(progress, 0.0F, 1.0F), degree);
    }
}