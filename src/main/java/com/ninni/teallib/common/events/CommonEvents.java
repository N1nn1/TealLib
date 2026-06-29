package com.ninni.teallib.common.events;

import com.ninni.teallib.TealLib;
import com.ninni.teallib.common.entity.Mannequin;
import com.ninni.teallib.registry.TealEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

import java.lang.reflect.InvocationTargetException;

@EventBusSubscriber(modid = TealLib.MODID)
public class CommonEvents {

    @SubscribeEvent
    public static void registerEntityAttribute(EntityAttributeCreationEvent event) {
        event.put(TealEntityType.MANNEQUIN.get(), Mannequin.createAttributes().build());
    }

    @SubscribeEvent
    public static void registerEntityAttribute(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            if (ModList.get().isLoaded("fieldguide")) {
                try {
                    Class<?> clazz = Class.forName("com.ninni.teallib.compat.fieldguide.FieldGuidePlugin");
                    clazz.getMethod("register").invoke(null);
                } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
