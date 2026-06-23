package com.ninni.teallib.common.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.biome.Biome;

import java.util.List;
import java.util.Optional;

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

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static void writeOptionalInt(FriendlyByteBuf buf, Optional<Integer> opt) {
        buf.writeBoolean(opt.isPresent());
        opt.ifPresent(buf::writeVarInt);
    }

    public static Optional<Integer> readOptionalInt(FriendlyByteBuf buf) {
        return buf.readBoolean() ? Optional.of(buf.readVarInt()) : Optional.empty();
    }


    public static class Location {
        public static final Codec<Location> CODEC = Codec.either(ResourceKey.codec(Registries.BIOME), TagKey.hashedCodec(Registries.BIOME)).xmap(Location::new, Location::get);
        private final Either<ResourceKey<Biome>, TagKey<Biome>> value;

        public Location(Either<ResourceKey<Biome>, TagKey<Biome>> value) {
            this.value = value;
        }

        public Either<ResourceKey<Biome>, TagKey<Biome>> get() {
            return value;
        }

        public boolean matchesBiome(Holder<Biome> biome) {
            if (value.left().isPresent()) {
                return biome.is(value.left().get());
            }
            if (value.right().isPresent()) {
                return biome.is(value.right().get());
            }
            return false;
        }

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        public static void writeLocation(FriendlyByteBuf buf, Optional<Location> location) {
            buf.writeBoolean(location.isPresent());
            location.ifPresent(location1 -> location1.get().ifLeft(
                    biome -> {
                        buf.writeBoolean(true);
                        buf.writeResourceLocation(biome.location());
                    }
            ).ifRight(
                    tag -> {
                        buf.writeBoolean(false);
                        buf.writeResourceLocation(tag.location());
                    }
            ));
        }

        public static Optional<Location> readLocation(FriendlyByteBuf buf) {
            if (!buf.readBoolean()) return Optional.empty();

            boolean isBiome = buf.readBoolean();
            ResourceLocation id = buf.readResourceLocation();

            return Optional.of(new Location(isBiome ? Either.left(ResourceKey.create(Registries.BIOME, id)) : Either.right(TagKey.create(Registries.BIOME, id))));
        }
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
