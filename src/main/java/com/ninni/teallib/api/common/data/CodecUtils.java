package com.ninni.teallib.api.common.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

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

    public static Codec<Float> floatRangeMinExclusiveWithMessage(float min, float max, Function<Float, String> errorMessage) {
        return Codec.FLOAT.validate((aFloat) -> aFloat.compareTo(min) >= 0 && aFloat.compareTo(max) <= 0 ? DataResult.success(aFloat) : DataResult.error(() -> errorMessage.apply(aFloat)));
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
