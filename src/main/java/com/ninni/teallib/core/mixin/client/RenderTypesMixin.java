package com.ninni.teallib.core.mixin.client;

import com.ninni.teallib.api.client.renderer.variant.VariantRenderContext;
import com.ninni.teallib.api.client.renderer.variant.VariantRenderManager;
import com.ninni.teallib.api.common.data.variant.VariantTextureSlot;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(RenderType.class)
public abstract class RenderTypesMixin {

    @Unique
    private static ResourceLocation teallib$texture(ResourceLocation original) {
        Entity entity = VariantRenderContext.get();
        VariantTextureSlot slot = VariantRenderContext.getSlot();
        if (entity == null || VariantRenderContext.shouldIgnoreTextureReplacement()) return original;
        if (slot == null) return VariantRenderManager.getTexture(entity, original);
        return VariantRenderManager.getTextureForSlot(entity, original, slot);
    }

    /** Swapping the argument keeps the vanilla body, so memoized types are reused and other retexturing mods still run. */
    @ModifyVariable(
            method = {
                    "entitySolid",
                    "entityCutout",
                    "entityTranslucentCull",
                    "entitySmoothCutout",
                    "entityDecal",
                    "entityNoOutline",
                    "eyes",
                    "energySwirl",
                    "breezeEyes",
                    "breezeWind",
                    "entityCutoutNoCull(Lnet/minecraft/resources/ResourceLocation;Z)Lnet/minecraft/client/renderer/RenderType;",
                    "entityCutoutNoCullZOffset(Lnet/minecraft/resources/ResourceLocation;Z)Lnet/minecraft/client/renderer/RenderType;",
                    "entityTranslucent(Lnet/minecraft/resources/ResourceLocation;Z)Lnet/minecraft/client/renderer/RenderType;",
                    "entityTranslucentEmissive(Lnet/minecraft/resources/ResourceLocation;Z)Lnet/minecraft/client/renderer/RenderType;"
            },
            at = @At("HEAD"),
            argsOnly = true,
            index = 0,
            require = 14,
            allow = 14
    )
    private static ResourceLocation teallib$replaceTexture(ResourceLocation textureLocation) {
        return teallib$texture(textureLocation);
    }
}
