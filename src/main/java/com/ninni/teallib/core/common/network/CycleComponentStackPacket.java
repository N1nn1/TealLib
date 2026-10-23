package com.ninni.teallib.core.common.network;

import com.ninni.teallib.api.common.item.ComponentStacking;
import com.ninni.teallib.core.TealLib;
import com.ninni.teallib.core.registry.TealSoundEvents;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CycleComponentStackPacket(int slotIndex, int direction, int step) implements CustomPacketPayload {
    public static final Type<CycleComponentStackPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "cycle_component_stack"));
    public static final StreamCodec<ByteBuf, CycleComponentStackPacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, CycleComponentStackPacket::slotIndex, ByteBufCodecs.VAR_INT, CycleComponentStackPacket::direction, ByteBufCodecs.VAR_INT, CycleComponentStackPacket::step, CycleComponentStackPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    public static void handle(CycleComponentStackPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            AbstractContainerMenu menu = player.containerMenu;
            int slotIndex = payload.slotIndex();
            if (slotIndex < 0 || slotIndex >= menu.slots.size()) return;
            Slot slot = menu.slots.get(slotIndex);
            ItemStack stack = slot.getItem();
            if (!ComponentStacking.isConfigured(stack) || stack.getCount() <= 1) return;
            int direction = Integer.signum(payload.direction());
            if (direction == 0 || !ComponentStacking.cycleMenuSlot(menu, slotIndex, direction, payload.step())) return;

            float pitch = switch (payload.step()) {
                case 4 -> 1.5F;
                case 16 -> 1.75F;
                default -> 1.35F;
            };
            player.playNotifySound(TealSoundEvents.CYCLE_ITEM.get(), SoundSource.MASTER, 0.2F, pitch);
            slot.setChanged();
            player.connection.send(new ClientboundContainerSetSlotPacket(menu.containerId, menu.getStateId(), slotIndex, slot.getItem()));
        });
    }
}
