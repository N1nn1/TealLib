package com.ninni.teallib.core.mixin.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.ninni.teallib.api.client.renderer.variant.VariantRenderContext;
import com.ninni.teallib.api.client.renderer.variant.VariantRenderManager;
import com.ninni.teallib.api.common.data.variant.VariantTextureSlot;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BiFunction;
import java.util.function.Function;

import static net.minecraft.client.renderer.RenderStateShard.*;

@Mixin(RenderType.class)
public abstract class RenderTypesMixin {
    @Shadow @Final public static Function<ResourceLocation, RenderType> ENTITY_SOLID;
    @Shadow @Final public static Function<ResourceLocation, RenderType> ENTITY_CUTOUT;
    @Shadow @Final public static Function<ResourceLocation, RenderType> ENTITY_TRANSLUCENT_CULL;
    @Shadow @Final public static Function<ResourceLocation, RenderType> ENTITY_SMOOTH_CUTOUT;
    @Shadow @Final public static Function<ResourceLocation, RenderType> ENTITY_DECAL;
    @Shadow @Final public static Function<ResourceLocation, RenderType> ENTITY_NO_OUTLINE;
    @Shadow @Final public static BiFunction<ResourceLocation, Boolean, RenderType> ENTITY_CUTOUT_NO_CULL;
    @Shadow @Final public static BiFunction<ResourceLocation, Boolean, RenderType> ENTITY_CUTOUT_NO_CULL_Z_OFFSET;
    @Shadow @Final public static BiFunction<ResourceLocation, Boolean, RenderType> ENTITY_TRANSLUCENT;
    @Shadow @Final public static BiFunction<ResourceLocation, Boolean, RenderType> ENTITY_TRANSLUCENT_EMISSIVE;
    @Shadow @Final public static BiFunction<ResourceLocation, RenderStateShard.TransparencyStateShard, RenderType> EYES;


    @Unique
    private static ResourceLocation teallib$texture(ResourceLocation original) {
        LivingEntity entity = VariantRenderContext.get();
        VariantTextureSlot slot = VariantRenderContext.getSlot();
        if (entity == null || VariantRenderContext.shouldIgnoreTextureReplacement()) return original;
        if (slot == null) return VariantRenderManager.getTexture(entity, original);
        return VariantRenderManager.getTextureForSlot(entity, original, slot);
    }

    @Inject(method = "entitySolid", at = @At("HEAD"), cancellable = true)
    private static void teallib$entitySolid(ResourceLocation textureLocation, CallbackInfoReturnable<RenderType> cir) {
        LivingEntity entity = VariantRenderContext.get();
        if (entity == null || VariantRenderContext.shouldIgnoreTextureReplacement()) return;
        cir.setReturnValue(ENTITY_SOLID.apply(teallib$texture(textureLocation)));
    }
    @Inject(method = "entityCutout", at = @At("HEAD"), cancellable = true)
    private static void teallib$entityCutout(ResourceLocation textureLocation, CallbackInfoReturnable<RenderType> cir) {
        LivingEntity entity = VariantRenderContext.get();
        if (entity == null || VariantRenderContext.shouldIgnoreTextureReplacement()) return;
        cir.setReturnValue(ENTITY_CUTOUT.apply(teallib$texture(textureLocation)));
    }
    @Inject(method = "entityTranslucentCull", at = @At("HEAD"), cancellable = true)
    private static void teallib$entityTranslucentCull(ResourceLocation textureLocation, CallbackInfoReturnable<RenderType> cir) {
        LivingEntity entity = VariantRenderContext.get();
        if (entity == null || VariantRenderContext.shouldIgnoreTextureReplacement()) return;
        cir.setReturnValue(ENTITY_TRANSLUCENT_CULL.apply(teallib$texture(textureLocation)));
    }
    @Inject(method = "entitySmoothCutout", at = @At("HEAD"), cancellable = true)
    private static void teallib$entitySmoothCutout(ResourceLocation textureLocation, CallbackInfoReturnable<RenderType> cir) {
        LivingEntity entity = VariantRenderContext.get();
        if (entity == null || VariantRenderContext.shouldIgnoreTextureReplacement()) return;
        cir.setReturnValue(ENTITY_SMOOTH_CUTOUT.apply(teallib$texture(textureLocation)));
    }
    @Inject(method = "entityDecal", at = @At("HEAD"), cancellable = true)
    private static void teallib$entityDecal(ResourceLocation textureLocation, CallbackInfoReturnable<RenderType> cir) {
        LivingEntity entity = VariantRenderContext.get();
        if (entity == null || VariantRenderContext.shouldIgnoreTextureReplacement()) return;
        cir.setReturnValue(ENTITY_DECAL.apply(teallib$texture(textureLocation)));
    }
    @Inject(method = "entityNoOutline", at = @At("HEAD"), cancellable = true)
    private static void teallib$entityNoOutline(ResourceLocation textureLocation, CallbackInfoReturnable<RenderType> cir) {
        LivingEntity entity = VariantRenderContext.get();
        if (entity == null || VariantRenderContext.shouldIgnoreTextureReplacement()) return;
        cir.setReturnValue(ENTITY_NO_OUTLINE.apply(teallib$texture(textureLocation)));
    }
    @Inject(method = "entityCutoutNoCull(Lnet/minecraft/resources/ResourceLocation;Z)Lnet/minecraft/client/renderer/RenderType;", at = @At("HEAD"), cancellable = true)
    private static void teallib$entityCutoutNoCull(ResourceLocation textureLocation, boolean noCull, CallbackInfoReturnable<RenderType> cir) {
        LivingEntity entity = VariantRenderContext.get();
        if (entity == null || VariantRenderContext.shouldIgnoreTextureReplacement()) return;
        cir.setReturnValue(ENTITY_CUTOUT_NO_CULL.apply(teallib$texture(textureLocation), noCull));
    }
    @Inject(method = "entityCutoutNoCullZOffset(Lnet/minecraft/resources/ResourceLocation;Z)Lnet/minecraft/client/renderer/RenderType;", at = @At("HEAD"), cancellable = true)
    private static void teallib$entityCutoutNoCullZOffset(ResourceLocation textureLocation, boolean noCull, CallbackInfoReturnable<RenderType> cir) {
        LivingEntity entity = VariantRenderContext.get();
        if (entity == null || VariantRenderContext.shouldIgnoreTextureReplacement()) return;
        cir.setReturnValue(ENTITY_CUTOUT_NO_CULL_Z_OFFSET.apply(teallib$texture(textureLocation), noCull));
    }
    @Inject(method = "entityTranslucent(Lnet/minecraft/resources/ResourceLocation;Z)Lnet/minecraft/client/renderer/RenderType;", at = @At("HEAD"), cancellable = true)
    private static void teallib$entityTranslucent(ResourceLocation textureLocation, boolean sortingEnabled, CallbackInfoReturnable<RenderType> cir) {
        LivingEntity entity = VariantRenderContext.get();
        if (entity == null || VariantRenderContext.shouldIgnoreTextureReplacement()) return;
        cir.setReturnValue(ENTITY_TRANSLUCENT.apply(teallib$texture(textureLocation), sortingEnabled));
    }
    @Inject(method = "entityTranslucentEmissive(Lnet/minecraft/resources/ResourceLocation;Z)Lnet/minecraft/client/renderer/RenderType;", at = @At("HEAD"), cancellable = true)
    private static void teallib$entityTranslucentEmissive(ResourceLocation textureLocation, boolean sortingEnabled, CallbackInfoReturnable<RenderType> cir) {
        LivingEntity entity = VariantRenderContext.get();
        if (entity == null || VariantRenderContext.shouldIgnoreTextureReplacement()) return;
        cir.setReturnValue(ENTITY_TRANSLUCENT_EMISSIVE.apply(teallib$texture(textureLocation), sortingEnabled));
    }
    @Inject(method = "eyes", at = @At("HEAD"), cancellable = true)
    private static void teallib$eyes(ResourceLocation textureLocation, CallbackInfoReturnable<RenderType> cir) {
        LivingEntity entity = VariantRenderContext.get();
        if (entity == null || VariantRenderContext.shouldIgnoreTextureReplacement()) return;
        cir.setReturnValue(EYES.apply(teallib$texture(textureLocation), RenderStateShard.ADDITIVE_TRANSPARENCY));
    }
    @Inject(method = "breezeEyes", at = @At("HEAD"), cancellable = true)
    private static void teallib$breezeEyes(ResourceLocation textureLocation, CallbackInfoReturnable<RenderType> cir) {
        LivingEntity entity = VariantRenderContext.get();
        if (entity == null || VariantRenderContext.shouldIgnoreTextureReplacement()) return;
        cir.setReturnValue(ENTITY_TRANSLUCENT_EMISSIVE.apply(teallib$texture(textureLocation), false));
    }
    @Inject(method = "breezeWind", at = @At("HEAD"), cancellable = true)
    private static void teallib$breezeWind(ResourceLocation textureLocation, float p_312776_, float p_312709_, CallbackInfoReturnable<RenderType> cir) {
        LivingEntity entity = VariantRenderContext.get();
        if (entity == null || VariantRenderContext.shouldIgnoreTextureReplacement()) return;
        cir.setReturnValue(RenderType.create("breeze_wind", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, false, true, RenderType.CompositeState.builder().setShaderState(RENDERTYPE_BREEZE_WIND_SHADER).setTextureState(new RenderStateShard.TextureStateShard(teallib$texture(textureLocation), false, false)).setTexturingState(new RenderStateShard.OffsetTexturingStateShard(p_312776_, p_312709_)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setCullState(NO_CULL).setLightmapState(LIGHTMAP).setOverlayState(NO_OVERLAY).createCompositeState(false)));
    }
}