package com.ninni.teallib.api.client.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;

public class AddParticlePacketClient {

    public static void handle(ParticleOptions particle, boolean alwaysRender, double x, double y, double z, double xV, double yV, double zV) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level != null) {
            if (alwaysRender) mc.level.addAlwaysVisibleParticle(particle, x, y, z, xV, yV, zV);
            else mc.level.addParticle(particle, x, y, z, xV, yV, zV);
        }
    }
}
