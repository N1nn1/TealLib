package com.ninni.teallib.core.common.network;

import com.ninni.teallib.api.common.item.ComponentStacking;
import com.ninni.teallib.core.TealLib;
import com.ninni.teallib.core.registry.TealSoundEvents;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CycleComponentStackPacket(int slotIndex, int direction) implements CustomPacketPayload {
    public static final Type<CycleComponentStackPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "cycle_component_stack"));
    public static final StreamCodec<ByteBuf, CycleComponentStackPacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, CycleComponentStackPacket::slotIndex, ByteBufCodecs.VAR_INT, CycleComponentStackPacket::direction, CycleComponentStackPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    public static void handle(CycleComponentStackPacket payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;
        AbstractContainerMenu menu = player.containerMenu;
        int slotIndex = payload.slotIndex();
        if (slotIndex < 0 || slotIndex >= menu.slots.size()) return;
        int direction = Integer.signum(payload.direction());
        if (direction == 0 || !ComponentStacking.cycleMenuSlot(menu, slotIndex, direction)) return;
        player.playNotifySound(TealSoundEvents.CYCLE_ITEM.get(), SoundSource.MASTER, 1.0F, 1.0F);
        menu.slots.get(slotIndex).setChanged();
        menu.broadcastFullState();
    }
}
