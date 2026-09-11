package com.ninni.teallib.core.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.ninni.teallib.core.TealLib;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

import java.io.IOException;

public final class TealShaders {
    public static final ResourceLocation ENTITY_UNLIT_EMISSIVE_LOCATION = ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "entity_unlit_emissive");

    public static ShaderInstance ENTITY_UNLIT_EMISSIVE;
    public static final RenderStateShard.ShaderStateShard ENTITY_UNLIT_EMISSIVE_SHADER = new RenderStateShard.ShaderStateShard(() -> ENTITY_UNLIT_EMISSIVE);


    public static void register(RegisterShadersEvent event) throws IOException {
        ShaderInstance shader = new ShaderInstance(event.getResourceProvider(), ENTITY_UNLIT_EMISSIVE_LOCATION, DefaultVertexFormat.NEW_ENTITY);
        event.registerShader(shader, loadedShader -> ENTITY_UNLIT_EMISSIVE = loadedShader);
    }
}