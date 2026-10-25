package com.ninni.teallib.api.client.network;

import com.ninni.teallib.core.client.event.ClientEvents;
import com.ninni.teallib.core.common.gui.ScreenShakeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;

public class HandleClientPackets {

    public static void handleAddParticle(ParticleOptions particle, boolean alwaysRender, double x, double y, double z, double xV, double yV, double zV) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level != null) {
            if (alwaysRender) mc.level.addAlwaysVisibleParticle(particle, x, y, z, xV, yV, zV);
            else mc.level.addParticle(particle, x, y, z, xV, yV, zV);
        }
    }

    public static void addScreenshake(ScreenShakeEvent event) {
        ClientEvents.SCREEN_SHAKE_EVENTS.add(event);
    }
}
