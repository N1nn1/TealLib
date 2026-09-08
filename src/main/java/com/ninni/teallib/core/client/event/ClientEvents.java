package com.ninni.teallib.core.client.event;

import com.ninni.teallib.api.client.particle.ShakingParticle;
import com.ninni.teallib.core.TealLib;
import com.ninni.teallib.core.client.entity.MannequinModel;
import com.ninni.teallib.core.client.entity.MannequinRenderer;
import com.ninni.teallib.api.client.renderer.item.CapturedMobsTooltipRenderer;
import com.ninni.teallib.api.common.item.tooltip.CapturedMobsTooltipData;
import com.ninni.teallib.core.registry.TealEntityType;
import com.ninni.teallib.core.registry.TealParticleType;
import net.minecraft.client.particle.AttackSweepParticle;
import net.minecraft.client.particle.CampfireSmokeParticle;
import net.minecraft.client.particle.HeartParticle;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = TealLib.MODID)
public class ClientEvents {

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(TealParticleType.STUN.get(), ShakingParticle.Factory::new);
    }

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
