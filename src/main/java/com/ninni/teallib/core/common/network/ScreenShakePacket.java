package com.ninni.teallib.core.common.network;

import com.ninni.teallib.api.client.network.HandleClientPackets;
import com.ninni.teallib.core.TealLib;
import com.ninni.teallib.core.common.gui.ScreenShakeEvent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public class ScreenShakePacket implements CustomPacketPayload {
    public static final Type<ScreenShakePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "screen_shake"));
    public static final StreamCodec<FriendlyByteBuf, ScreenShakePacket> STREAM_CODEC = StreamCodec.of(ScreenShakePacket::write, ScreenShakePacket::read);

    private final Vec3 position;
    private final int duration;
    private final float degree;
    private final float distance;
    private final boolean groundRequired;

    public ScreenShakePacket(Vec3 position, int duration, float degree, float distance, boolean groundRequired) {
        this.position = position;
        this.duration = duration;
        this.degree = degree;
        this.distance = distance;
        this.groundRequired = groundRequired;
    }

    public static void write(FriendlyByteBuf buffer, ScreenShakePacket object) {
        buffer.writeVec3(object.position);
        buffer.writeInt(object.duration);
        buffer.writeFloat(object.degree);
        buffer.writeFloat(object.distance);
        buffer.writeBoolean(object.groundRequired);
    }

    public static ScreenShakePacket read(FriendlyByteBuf buffer) {
        return new ScreenShakePacket(buffer.readVec3(), buffer.readInt(), buffer.readFloat(), buffer.readFloat(), buffer.readBoolean());
    }

    public static void screenShake(ServerLevel level, Vec3 position, int duration, float degree, float distance, boolean groundRequired) {
        PacketDistributor.sendToPlayersNear(level, null, position.x ,position.y, position.z, distance * 1.2f, new ScreenShakePacket(position, duration, degree, distance, groundRequired));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ScreenShakePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            HandleClientPackets.addScreenshake(new ScreenShakeEvent(packet.position, packet.duration, packet.degree, packet.distance, packet.groundRequired));
        });
    }
}
