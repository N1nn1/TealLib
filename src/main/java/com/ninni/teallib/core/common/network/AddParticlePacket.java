package com.ninni.teallib.core.common.network;

import com.ninni.teallib.api.client.network.AddParticlePacketClient;
import com.ninni.teallib.core.TealLib;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public class AddParticlePacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<AddParticlePacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "add_particle"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AddParticlePacket> STREAM_CODEC = StreamCodec.of(AddParticlePacket::write, AddParticlePacket::read);

    public final ParticleOptions particle;
    public final boolean alwaysRender;
    public final double x;
    public final double y;
    public final double z;
    public final double xV;
    public final double yV;
    public final double zV;

    public <T extends ParticleOptions>AddParticlePacket(T particle, boolean alwaysRender, double x, double y, double z, double xV, double yV, double zV) {
        this.particle = particle;
        this.alwaysRender = alwaysRender;
        this.x = x;
        this.y = y;
        this.z = z;
        this.xV = xV;
        this.yV = yV;
        this.zV = zV;
    }

    public static void write(RegistryFriendlyByteBuf buffer, AddParticlePacket object) {
        ParticleTypes.STREAM_CODEC.encode(buffer, object.particle);
        buffer.writeBoolean(object.alwaysRender);
        buffer.writeDouble(object.x);
        buffer.writeDouble(object.y);
        buffer.writeDouble(object.z);
        buffer.writeDouble(object.xV);
        buffer.writeDouble(object.yV);
        buffer.writeDouble(object.zV);
    }

    public static AddParticlePacket read(RegistryFriendlyByteBuf buffer) {
        return new AddParticlePacket(
                ParticleTypes.STREAM_CODEC.decode(buffer),
                buffer.readBoolean(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble()
        );
    }

    public static void addParticle(ServerLevel level, int radius, ParticleOptions options, Vec3 pos, Vec3 vel) {
        addParticle(level, radius, options, false, pos, vel);
    }

    public static void addParticle(ServerLevel level, int radius, ParticleOptions options, boolean b, Vec3 pos, Vec3 vel) {
        addParticle(level, radius, options, b, pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);
    }

    public static void addParticle(ServerLevel level, int radius, ParticleOptions options, double x, double y, double z, double xV, double yV, double zV) {
        addParticle(level, radius, options, false, x, y, z, x, y, z);
    }

    public static void addParticle(ServerLevel level, int radius, ParticleOptions options, boolean b, double x, double y, double z, double xV, double yV, double zV) {
        PacketDistributor.sendToPlayersNear(level, null, x ,y, z, radius, new AddParticlePacket(options, b, x ,y, z, xV, yV, zV));
    }

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AddParticlePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> AddParticlePacketClient.handle(packet.particle, packet.alwaysRender, packet.x, packet.y, packet.z, packet.xV, packet.yV, packet.zV));
    }
}
