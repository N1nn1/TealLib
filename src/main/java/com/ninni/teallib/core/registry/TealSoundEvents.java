package com.ninni.teallib.core.registry;

import com.ninni.teallib.core.TealLib;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.DeferredSoundType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Optional;

public final class TealSoundEvents {

    public static final DeferredRegister<SoundEvent> DEF_REG = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, TealLib.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> TITAN_MANNEQUIN_IDLE = register("entity.titan_mannequin.idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> TITAN_MANNEQUIN_HURT = register("entity.titan_mannequin.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> TITAN_MANNEQUIN_DEATH = register("entity.titan_mannequin.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> TITAN_MANNEQUIN_STEP = register("entity.titan_mannequin.step");

    public static final DeferredHolder<SoundEvent, SoundEvent> CYCLE_ITEM = register("ui.item.cycle");


    public static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(TealLib.MODID, name);
        return DEF_REG.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }
}