package com.ninni.teallib.core.client.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Either;
import com.ninni.teallib.api.client.particle.ShakingParticle;
import com.ninni.teallib.api.client.renderer.item.CombinedStackBarRenderer;
import com.ninni.teallib.api.common.item.ComponentStacking;
import com.ninni.teallib.core.TealLib;
import com.ninni.teallib.core.client.TealShaders;
import com.ninni.teallib.core.client.entity.MannequinModel;
import com.ninni.teallib.core.client.entity.MannequinRenderer;
import com.ninni.teallib.api.client.renderer.item.CapturedMobsTooltipRenderer;
import com.ninni.teallib.api.common.item.tooltip.CapturedMobsTooltipData;
import com.ninni.teallib.core.common.gui.ScreenShakeEvent;
import com.ninni.teallib.core.common.item.CombinedStackBarData;
import com.ninni.teallib.core.common.network.CycleComponentStackPacket;
import com.ninni.teallib.core.mixin.client.AbstractContainerScreenAccessor;
import com.ninni.teallib.core.registry.TealDataComponentTypes;
import com.ninni.teallib.core.registry.TealEntityType;
import com.ninni.teallib.core.registry.TealParticleType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.network.PacketDistributor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@EventBusSubscriber(value = Dist.CLIENT, modid = TealLib.MODID)
public class ClientEvents {
    public static final List<ScreenShakeEvent> SCREEN_SHAKE_EVENTS = new ArrayList<>();
    private static float shakeAmount;
    private static float prevShakeAmount;


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
        PacketDistributor.sendToServer(new CycleComponentStackPacket(slotIndex, direction, Screen.hasControlDown() ? 16 : Screen.hasShiftDown() ? 4 : 1));
    }


    @SubscribeEvent
    public static void addStackVariantBar(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || !ComponentStacking.hasMultipleVariants(stack) || !stack.has(TealDataComponentTypes.STACK_VARIANT_INDEX.get())) return;
        int index = stack.getOrDefault(TealDataComponentTypes.STACK_VARIANT_INDEX.get(), 0);
        event.getTooltipElements().add(Either.right(new CombinedStackBarData(index, stack.getCount())));
    }


    @SubscribeEvent
    public static void clientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        //Code taken and modified from Alex
        Entity cameraEntity = mc.getCameraEntity();
        prevShakeAmount = shakeAmount;
        float shake = 0.0F;
        Iterator<ScreenShakeEvent> groundShakeMomentIterator = SCREEN_SHAKE_EVENTS.iterator();
        while (groundShakeMomentIterator.hasNext()) {
            ScreenShakeEvent groundShakeMoment = groundShakeMomentIterator.next();
            groundShakeMoment.tick();
            if (groundShakeMoment.isDone()) groundShakeMomentIterator.remove();
            else shake = Math.max(shake, groundShakeMoment.getDegree(cameraEntity, 1.0F));
        }
        shakeAmount = shake * mc.options.screenEffectScale().get().floatValue();
    }

    @SubscribeEvent
    public static void renderScreenShake(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        float partialTicks = (float) event.getPartialTick();

        //Code taken and modified from Alex
        float lerpedShakeAmount = Mth.clamp(prevShakeAmount + (shakeAmount - prevShakeAmount) * partialTicks, 0, 4.0F);
        if (lerpedShakeAmount > 0) {
            float time = mc.cameraEntity == null ? 0.0F : mc.cameraEntity.tickCount + mc.getTimer().getGameTimeDeltaPartialTick(false);
            event.setRoll((float) (lerpedShakeAmount * Math.sin(2.0F * time)));
        }
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
        registry.register(CombinedStackBarData.class, CombinedStackBarRenderer::new);
    }
}
