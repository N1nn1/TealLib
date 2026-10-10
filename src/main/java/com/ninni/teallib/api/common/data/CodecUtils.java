package com.ninni.teallib.api.common.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

import java.util.List;
import java.util.function.Function;

/**
 * A collection of codecs and helper types used throughout Teal Lib's
 * data-driven systems.
 * <p>
 * These utilities provide common serialization formats for variant
 * configuration, including biome restrictions, weather conditions
 * and custom name matching.
 */
public class CodecUtils {

    public record NameTagRule(List<String> names, boolean ignoreCase) {
        public static final Codec<NameTagRule> CODEC = RecordCodecBuilder.create(i ->
                i.group(
                        Codec.STRING.listOf().fieldOf("names").forGetter(NameTagRule::names),
                        Codec.BOOL.optionalFieldOf("ignoreCase", true).forGetter(NameTagRule::ignoreCase)
                ).apply(i, NameTagRule::new)
        );

        public boolean matches(Component customName) {
            String s = ChatFormatting.stripFormatting(customName.getString());
            for (String n : names) if (ignoreCase ? s.equalsIgnoreCase(n) : s.equals(n)) return true;
            return false;
        }
    }

    public record BiomeSelector(ResourceLocation id, boolean tag) {
        public static final Codec<BiomeSelector> CODEC =
                Codec.STRING.comapFlatMap(
                        input -> {
                            boolean isTag = input.startsWith("#");
                            String value = isTag ? input.substring(1) : input;
                            if (value.isEmpty()) {
                                return DataResult.error(() -> "Biome ID or tag cannot be empty: " + input);
                            }
                            return ResourceLocation.read(value).map(id -> new BiomeSelector(id, isTag));
                        },
                        selector -> (selector.tag() ? "#" : "") + selector.id()
                );

        public static final Codec<List<BiomeSelector>> LIST_OR_SINGLE_CODEC = Codec.either(CODEC, CODEC.listOf()).xmap(either -> either.map(List::of, list -> list), list -> list.size() == 1 ? Either.left(list.getFirst()) : Either.right(list));

        public boolean matches(Holder<Biome> biome) {
            if (tag) {
                TagKey<Biome> tagKey = TagKey.create(Registries.BIOME, id);
                return biome.is(tagKey);
            }
            ResourceKey<Biome> biomeKey = ResourceKey.create(Registries.BIOME, id);
            return biome.is(biomeKey);
        }
    }

    public static Codec<Float> floatRangeMinExclusiveWithMessage(float min, float max, Function<Float, String> errorMessage) {
        return Codec.FLOAT.validate((aFloat) -> aFloat.compareTo(min) >= 0 && aFloat.compareTo(max) <= 0 ? DataResult.success(aFloat) : DataResult.error(() -> errorMessage.apply(aFloat)));
    }

    public static CodecUtils.Weather resolveWeather(LevelReader level, BlockPos pos, Holder<Biome> biome) {
        Level weatherLevel;

        if (level instanceof Level l) weatherLevel = l;
        else if (level instanceof ServerLevelAccessor accessor) weatherLevel = accessor.getLevel();
        else return CodecUtils.Weather.NONE;

        if (!weatherLevel.isRaining()) return CodecUtils.Weather.NONE;
        if (!level.canSeeSky(pos)) return CodecUtils.Weather.NONE;
        if (level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos).getY() > pos.getY()) return CodecUtils.Weather.NONE;

        Biome.Precipitation precipitation = biome.value().getPrecipitationAt(pos);
        if (precipitation == Biome.Precipitation.SNOW) return CodecUtils.Weather.SNOW;

        if (precipitation == Biome.Precipitation.RAIN) {
            return weatherLevel.isThundering() ? CodecUtils.Weather.THUNDER : CodecUtils.Weather.RAIN;
        }

        return CodecUtils.Weather.NONE;
    }

    public enum Weather implements StringRepresentable {
        NONE("none"),
        RAIN("rain"),
        SNOW("snow"),
        THUNDER("thunder");

        public static final Codec<Weather> CODEC = StringRepresentable.fromEnum(Weather::values);
        private final String name;

        Weather(String name) {
            this.name = name;
        }
        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
