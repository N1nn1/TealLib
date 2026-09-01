package com.ninni.teallib.core.common.event;

import com.ninni.teallib.api.common.data.variant.VariantManager;
import com.ninni.teallib.core.TealLib;
import com.ninni.teallib.core.common.entity.Mannequin;
import com.ninni.teallib.core.registry.TealEntityType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

import java.lang.reflect.InvocationTargetException;

@EventBusSubscriber(modid = TealLib.MODID)
public class CommonEvents {

    @SubscribeEvent
    public static void registerEntityAttribute(EntityAttributeCreationEvent event) {
        event.put(TealEntityType.MANNEQUIN.get(), Mannequin.createAttributes().build());
    }

    @SubscribeEvent
    public static void applyVariants(FinalizeSpawnEvent event) {
        if (!event.isSpawnCancelled() && event.getSpawnType() != MobSpawnType.BUCKET) {
            ResourceLocation entityType = BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType());

            if (TealLib.COMMON_CONFIG.variantNamespaceBlacklist.get().contains(entityType.getNamespace())) return;
            boolean contains = false;
            for (String string : TealLib.COMMON_CONFIG.variantBlacklist.get()) {
                if (entityType.toString().equals(string)) {
                    contains = true;
                    break;
                }
            }
            if (contains) return;

            VariantManager.assignNaturally(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            if (ModList.get().isLoaded("fieldguide")) {
                try {
                    Class<?> clazz = Class.forName("com.ninni.teallib.core.compat.fieldguide.FieldGuidePlugin");
                    clazz.getMethod("register").invoke(null);
                } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
