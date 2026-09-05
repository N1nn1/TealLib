package com.ninni.teallib.core.mixin.client;

import com.ninni.teallib.api.client.renderer.variant.VariantRenderContext;
import com.ninni.teallib.api.client.renderer.variant.VariantRenderManager;
import com.ninni.teallib.api.common.data.variant.VariantTextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.client.NeoForgeRenderTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(NeoForgeRenderTypes.class)
public abstract class NeoForgeRenderTypesMixin {

    @Unique
    private static ResourceLocation teallib$texture(ResourceLocation original) {
        Entity entity = VariantRenderContext.get();
        VariantTextureSlot slot = VariantRenderContext.getSlot();
        if (entity == null || VariantRenderContext.shouldIgnoreTextureReplacement()) return original;
        if (slot == null) return VariantRenderManager.getTexture(entity, original);
        return VariantRenderManager.getTextureForSlot(entity, original, slot);
    }

    @ModifyVariable(
            method = {
                    "getEntityCutoutMipped(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;",
                    "getUnsortedTranslucent(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;",
                    "getUnlitTranslucent(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;",
                    "getUnlitTranslucent(Lnet/minecraft/resources/ResourceLocation;Z)Lnet/minecraft/client/renderer/RenderType;"
            },
            at = @At("HEAD"),
            argsOnly = true,
            index = 0,
            require = 4,
            allow = 4
    )
    private static ResourceLocation teallib$replaceTexture(ResourceLocation textureLocation) {
        return teallib$texture(textureLocation);
    }
}
