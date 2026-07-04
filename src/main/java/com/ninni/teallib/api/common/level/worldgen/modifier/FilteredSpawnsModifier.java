package com.ninni.teallib.api.common.level.worldgen.modifier;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.MobSpawnSettingsBuilder;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;

import java.util.List;
import java.util.function.Function;


/**
 *  Biome modifier that adds mob spawns to a list of biomes, excluding some.
 *  Useful for things like adding hostile mob spawns to every overworld biome besides {@code Mushroom Fields} or {@code Deep Dark}
 */
public record FilteredSpawnsModifier(
        HolderSet<Biome> biomes,
        HolderSet<Biome> filtered,
        List<MobSpawnSettings.SpawnerData> spawners
) implements BiomeModifier {

    public static final MapCodec<FilteredSpawnsModifier> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(FilteredSpawnsModifier::biomes),
            Biome.LIST_CODEC.fieldOf("filtered").forGetter(FilteredSpawnsModifier::filtered),
            Codec.either(MobSpawnSettings.SpawnerData.CODEC.listOf(), MobSpawnSettings.SpawnerData.CODEC).xmap(
                    either -> either.map(Function.identity(), List::of),
                    list -> list.size() == 1 ? Either.right(list.get(0)) : Either.left(list)
            ).fieldOf("spawners").forGetter(FilteredSpawnsModifier::spawners)
    ).apply(builder, FilteredSpawnsModifier::new));

    @Override
    public void modify(Holder<Biome> biome, BiomeModifier.Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase == Phase.ADD && this.biomes.contains(biome) && !this.filtered.contains(biome)) {
            MobSpawnSettingsBuilder spawns = builder.getMobSpawnSettings();
            for (MobSpawnSettings.SpawnerData spawner : this.spawners) {
                EntityType<?> type = spawner.type;
                spawns.addSpawn(type.getCategory(), spawner);
            }
        }
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return CODEC;
    }
}