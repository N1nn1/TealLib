package com.ninni.teallib.core.registry;

import com.ninni.teallib.core.TealLib;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Optional;

public final class TealAttachments {

    public static final DeferredRegister<AttachmentType<?>> DEF_REG = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, TealLib.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Optional<ResourceLocation>>> VARIANT = DEF_REG.register(
            "variant",
            () -> AttachmentType
                    .<Optional<ResourceLocation>>builder(Optional::empty)
                    .serialize(ResourceLocation.CODEC.optionalFieldOf("variant").codec())
                    .sync(ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC))
                    .build()
    );
}