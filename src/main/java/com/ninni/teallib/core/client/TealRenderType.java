package com.ninni.teallib.core.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.ninni.teallib.api.client.renderer.variant.VariantRenderContext;
import com.ninni.teallib.api.client.renderer.variant.VariantRenderManager;
import com.ninni.teallib.api.common.data.variant.VariantTextureSlot;

import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.function.Function;

public class TealRenderType extends RenderType {

    public TealRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    public static final Function<ResourceLocation, RenderType> UNLIT_TRANSLUCENT_EMISSIVE =
            Util.memoize(texture -> {
                CompositeState state = CompositeState.builder()
                        .setShaderState(TealShaders.ENTITY_UNLIT_EMISSIVE_SHADER)
                        .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setCullState(NO_CULL)
                        .setLightmapState(LIGHTMAP)
                        .setOverlayState(OVERLAY)
                        .setWriteMaskState(COLOR_WRITE)
                        .createCompositeState(true);
                return create("teallib_entity_unlit_translucent_emissive", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, true, true, state);
            });

    public static RenderType unlitTranslucentEmissive(ResourceLocation id) {
        return UNLIT_TRANSLUCENT_EMISSIVE.apply(getTealLibTexture(id));
    }

    public static ResourceLocation getTealLibTexture(ResourceLocation id) {
        Entity entity = VariantRenderContext.get();
        VariantTextureSlot slot = VariantRenderContext.getSlot();
        if (entity == null || VariantRenderContext.shouldIgnoreTextureReplacement()) return id;
        if (slot == null) return VariantRenderManager.getTexture(entity, id);
        return VariantRenderManager.getTextureForSlot(entity, id, slot);
    }
}