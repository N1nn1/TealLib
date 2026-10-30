package com.ninni.teallib.core.registry;

import com.ninni.teallib.core.TealLib;
import com.ninni.teallib.core.common.item.MannequinItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class TealTags {
    public static final TagKey<Item> COMPONENT_STACKABLE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "component_stackable"));

    public static final TagKey<EntityType<?>> ICE_SLIPPING_IMMUNE = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "ice_slipping_immune"));

    public static TagKey<Biome> DOESNT_FREEZE = TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "doesnt_freeze"));
    public static TagKey<Biome> DOESNT_SNOW = TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "doesnt_snow"));
}
