package com.ninni.teallib.core.registry;

import com.ninni.teallib.core.TealLib;
import com.ninni.teallib.core.common.entity.Mannequin;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class TealEntityType {
    public static final DeferredRegister<EntityType<?>> DEF_REG = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, TealLib.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<Mannequin>> MANNEQUIN = DEF_REG.register(
            "mannequin",
            () -> EntityType.Builder.of(Mannequin::new, MobCategory.MISC)
                    .sized(0.5F, 1.975F).eyeHeight(1.7775F).clientTrackingRange(10)
                    .build(ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "mannequin").toString())
    );

}
