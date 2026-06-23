package com.ninni.teallib.server.network;

import com.ninni.teallib.TealLib;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public class BlockEntitySyncPacket implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<BlockEntitySyncPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "block_entity_sync"));

    public static final StreamCodec<FriendlyByteBuf, BlockEntitySyncPacket> STREAM_CODEC =
            StreamCodec.of(BlockEntitySyncPacket::write, BlockEntitySyncPacket::read);

    final BlockPos pos;
    final CompoundTag tag;

    public BlockEntitySyncPacket(BlockPos pos, CompoundTag tag) {
        this.pos = pos;
        this.tag = tag;
    }

    public static void write(FriendlyByteBuf buffer, BlockEntitySyncPacket object) {
        buffer.writeBlockPos(object.pos);
        buffer.writeNbt(object.tag);
    }

    public static BlockEntitySyncPacket read(FriendlyByteBuf buffer) {
        return new BlockEntitySyncPacket(buffer.readBlockPos(), buffer.readNbt());
    }

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(BlockEntitySyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Level world = context.player().level();

            BlockEntity t = world.getBlockEntity(packet.pos);
            if (t != null) {
                t.loadWithComponents(packet.tag, world.registryAccess());
                t.setChanged();
            }
        });
    }
}
