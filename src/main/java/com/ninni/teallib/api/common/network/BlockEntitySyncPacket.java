package com.ninni.teallib.api.common.network;

import com.ninni.teallib.core.TealLib;
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

/**
 * A network packet used to synchronize a Block Entity's full state from server to client.
 * <p>
 * This packet carries the BlockPos of the target Block Entity and an NBT payload
 * representing its serialized state. When received on the client, the Block Entity
 * is reloaded using {@code loadWithComponents}, ensuring its state matches the server.
 * <p>
 * This is primarily used for custom Block Entity systems that require manual
 * synchronization beyond (like the mod) vanilla update packets.
 */
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
