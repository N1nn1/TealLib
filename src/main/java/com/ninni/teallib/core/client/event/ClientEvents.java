package com.ninni.teallib.core.client.event;

import com.ninni.teallib.api.client.particle.ShakingParticle;
import com.ninni.teallib.api.common.item.ComponentStacking;
import com.ninni.teallib.core.TealLib;
import com.ninni.teallib.core.client.TealShaders;
import com.ninni.teallib.core.client.entity.MannequinModel;
import com.ninni.teallib.core.client.entity.MannequinRenderer;
import com.ninni.teallib.api.client.renderer.item.CapturedMobsTooltipRenderer;
import com.ninni.teallib.api.common.item.tooltip.CapturedMobsTooltipData;
import com.ninni.teallib.core.common.network.CycleComponentStackPacket;
import com.ninni.teallib.core.mixin.client.AbstractContainerScreenAccessor;
import com.ninni.teallib.core.registry.TealEntityType;
import com.ninni.teallib.core.registry.TealParticleType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.io.IOException;

@EventBusSubscriber(value = Dist.CLIENT, modid = TealLib.MODID)
public class ClientEvents {

    @SubscribeEvent
    public static void onAdditional(ModelEvent.RegisterAdditional event) {
        ResourceManager resources = Minecraft.getInstance().getResourceManager();

        for (ResourceLocation resource : resources.listResources("models/item/tealvariant", location -> location.getPath().endsWith(".json")).keySet()) {
            String modelPath = resource.getPath().substring("models/".length(), resource.getPath().length() - ".json".length());
            ResourceLocation modelId = ResourceLocation.fromNamespaceAndPath(resource.getNamespace(), modelPath);
            TealLib.LOGGER.debug("Registering variant model {}", modelId);
            event.register(ModelResourceLocation.standalone(modelId));
        }
    }

    @SubscribeEvent
    public static void scroll(ScreenEvent.MouseScrolled.Post event) {
        if (event.getScrollDeltaY() == 0 || !(event.getScreen() instanceof AbstractContainerScreen<?> screen)) return;
        Slot slot = ((AbstractContainerScreenAccessor) screen).t$findSlot(event.getMouseX(), event.getMouseY());
        if (slot == null || !ComponentStacking.isConfigured(slot.getItem()) || slot.getItem().getCount() <= 1) return;
        int slotIndex = screen.getMenu().slots.indexOf(slot);
        if (slotIndex < 0) return;
        int direction = event.getScrollDeltaY() > 0 ? 1 : -1;
        PacketDistributor.sendToServer(new CycleComponentStackPacket(slotIndex, direction));
    }


    @SubscribeEvent
    public static void register(RegisterShadersEvent event) throws IOException {
        TealShaders.register(event);
    }

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
