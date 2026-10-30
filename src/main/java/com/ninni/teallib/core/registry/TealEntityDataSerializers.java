package com.ninni.teallib.core.registry;

import com.ninni.teallib.api.common.entity.sound.SoundPersonality;
import com.ninni.teallib.core.TealLib;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class TealEntityDataSerializers {
    public static final DeferredRegister<EntityDataSerializer<?>> DEF_REG = DeferredRegister.create(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS, TealLib.MODID);

    public static final Supplier<EntityDataSerializer<List<String>>> STRING_LIST = DEF_REG.register("string_list", () -> EntityDataSerializer.forValueType(ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list())));
    public static final Supplier<EntityDataSerializer<List<UUID>>> UUID_LIST = DEF_REG.register("uuid_list", () -> EntityDataSerializer.forValueType(UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs.list())));
    public static final Supplier<EntityDataSerializer<SoundPersonality>> SOUND_PERSONALITY = DEF_REG.register("sound_personality", () -> EntityDataSerializer.forValueType(SoundPersonality.STREAM_CODEC));

}
