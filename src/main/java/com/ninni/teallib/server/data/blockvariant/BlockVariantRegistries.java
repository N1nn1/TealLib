package com.ninni.teallib.server.data.blockvariant;

import com.ninni.teallib.TealLib;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

@EventBusSubscriber(modid = TealLib.MODID)
public class BlockVariantRegistries {
    public static final ResourceKey<Registry<BlockVariantManager.BlockVariantData>> BLOCK_VARIANT_REGISTRY_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "block_variants"));

    @SubscribeEvent
    public static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(
                BLOCK_VARIANT_REGISTRY_KEY,
                BlockVariantManager.BlockVariantData.CODEC,
                BlockVariantManager.BlockVariantData.CODEC
        );
    }
}