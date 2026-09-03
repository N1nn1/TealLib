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
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import com.ninni.teallib.api.common.data.variant.VariantDefinition;
import com.ninni.teallib.api.common.data.variant.VariantTarget;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

            VariantManager.assignNaturally(event.getEntity(), event.getLevel());
        }
    }

    /** Cheapest way to tell a broken pack apart from a broken renderer when a variant does not show. */
    @SubscribeEvent
    public static void logVariantRegistry(ServerStartedEvent event) {
        if (!TealLib.LOGGER.isDebugEnabled()) return;
        List<VariantDefinition> all = VariantManager.all(event.getServer().registryAccess());
        Set<VariantTarget> targets = new HashSet<>();
        for (VariantDefinition variant : all) targets.addAll(variant.targets());
        TealLib.LOGGER.debug("Loaded {} json variant definitions across {} targets", all.size(), targets.size());
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
