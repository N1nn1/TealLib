package com.ninni.teallib.core;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class TealLibCommonConfig {
    public final ModConfigSpec.ConfigValue<List<String>> variantBlacklist;
    public final ModConfigSpec.ConfigValue<List<String>> variantNamespaceBlacklist;


    public TealLibCommonConfig(final ModConfigSpec.Builder builder) {

        builder.translation("config.teallib.section.json_variants").push("json_variants");

        variantBlacklist = builder
                .comment("""
                        Add here the entity id's of mobs you don't want json variants applied to.
                        Teal Lib's variants can be aggressive and replace intended entity variants,
                        but it really shouldn't matter unless a variant datapack is enabled.
                        Default: ["minecraft:ender_dragon", "minecraft:wolf", "minecraft:horse", "minecraft:tropical_fish"]
                        """)
                .translation("config.teallib.variant_blacklist")
                .define("variant_blacklist", List.of(
                        BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.ENDER_DRAGON).toString(),
                        BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.WOLF).toString(),
                        BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.HORSE).toString(),
                        BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.TROPICAL_FISH).toString()
                ));

        variantNamespaceBlacklist = builder
                .comment("""
                        A blacklist for entire mod id's, entities from these mods will not have json variants.
                        By default, only includes "minecraft", because default variants for the base game's mobs
                        are registered by Teal Lib for easy datapack making, but if you're not using any datapacks
                        they will replace modded variants for them.
                        Default: ["minecraft"]
                        """)
                .translation("config.teallib.variant_namespace_blacklist")
                .define("variant_namespace_blacklist", List.of("minecraft"));
        builder.pop();
    }
}
