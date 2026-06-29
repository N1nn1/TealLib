package com.ninni.teallib.client.event;

import com.ninni.teallib.TealLib;
import com.ninni.teallib.client.entity.MannequinModel;
import com.ninni.teallib.client.entity.MannequinRenderer;
import com.ninni.teallib.client.renderer.item.CapturedMobsTooltipRenderer;
import com.ninni.teallib.common.item.tooltip.CapturedMobsTooltipData;
import com.ninni.teallib.registry.TealEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = TealLib.MODID)
public class ClientEvents {

    @SubscribeEvent
    public static void registerEntityLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(MannequinModel.LAYER_LOCATION, MannequinModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(TealEntityType.MANNEQUIN.get(), MannequinRenderer::new);
    }

    @SubscribeEvent
    public static void registerTooltips(RegisterClientTooltipComponentFactoriesEvent registry) {
        registry.register(CapturedMobsTooltipData.class, CapturedMobsTooltipRenderer::new);
    }
}
