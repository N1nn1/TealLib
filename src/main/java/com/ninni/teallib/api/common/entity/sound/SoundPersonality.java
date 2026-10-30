package com.ninni.teallib.api.common.entity.sound;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ninni.teallib.core.TealLib;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public record SoundPersonality(ResourceLocation id, Map<String, ResourceLocation> soundDefinitions) {
    public static final MapCodec<SoundPersonality> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(SoundPersonality::id),
            Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC).fieldOf("sound_definitions").forGetter(SoundPersonality::soundDefinitions)
    ).apply(inst, SoundPersonality::new));

    public static final StreamCodec<FriendlyByteBuf, SoundPersonality> STREAM_CODEC = StreamCodec.of(SoundPersonality::write, SoundPersonality::read);
    public static final ResourceLocation DEFAULT = ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "default");

    private static SoundPersonality read(FriendlyByteBuf friendlyByteBuf) {
        return new SoundPersonality(friendlyByteBuf.readResourceLocation(), friendlyByteBuf.readMap(FriendlyByteBuf::readUtf, FriendlyByteBuf::readResourceLocation));
    }

    private static void write(FriendlyByteBuf friendlyByteBuf, SoundPersonality personality) {
        friendlyByteBuf.writeResourceLocation(personality.id);
        friendlyByteBuf.writeMap(personality.soundDefinitions, FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeResourceLocation);
    }


    @Nullable
    public SoundEvent getSoundEvent(String name) {
        return BuiltInRegistries.SOUND_EVENT.get(this.soundDefinitions.get(name));
    }

    public static SoundPersonality loadFromTag(CompoundTag tag) {
        HashMap<String, ResourceLocation> soundDefinitions = new HashMap<>();
        ResourceLocation id = null;

        if (tag.contains("sound_variant")) {
            CompoundTag personality = tag.getCompound("sound_variant");
            id = ResourceLocation.tryParse(personality.getString("id"));
            for (String key : personality.getAllKeys()) {
                ResourceLocation value = ResourceLocation.tryParse(personality.getString(key));
                if (value == null) TealLib.LOGGER.error("Sound Personality Sound Event for {} key is null, could not be loaded", key);
                else soundDefinitions.put(key, value);
            }
        }
        if (id == null) {
            TealLib.LOGGER.error("Sound Personality is missing an id, using {} instead", DEFAULT);
            id = DEFAULT;
        }
        return new SoundPersonality(id, soundDefinitions);
    }

    public void saveToTag(CompoundTag tag) {
        CompoundTag personality = new CompoundTag();
        personality.putString("id", id.toString());
        for (String name : soundDefinitions.keySet()) {
            personality.putString(name, soundDefinitions.get(name).toString());
        }
        tag.put("sound_variant", personality);
    }

}
