package com.ninni.teallib.api.common.data.entityvariant;

import com.ninni.teallib.core.TealLib;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

@EventBusSubscriber(modid = TealLib.MODID)
public class EntityVariantRegistries {
    public static final ResourceKey<Registry<EntityVariantManager.EntityVariantData>> ENTITY_VARIANT_REGISTRY_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "entity_variants"));

    @SubscribeEvent
    public static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(
                ENTITY_VARIANT_REGISTRY_KEY,
                EntityVariantManager.EntityVariantData.CODEC,
                EntityVariantManager.EntityVariantData.CODEC
        );
    }
}