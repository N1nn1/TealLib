package com.ninni.teallib.registry;

import com.ninni.teallib.TealLib;
import com.ninni.teallib.server.network.BlockEntitySyncPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = TealLib.MODID)
public class TealNetwork {

    @SubscribeEvent
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(TealLib.MODID);

        registrar.playToClient(BlockEntitySyncPacket.TYPE, BlockEntitySyncPacket.STREAM_CODEC, BlockEntitySyncPacket::handle);
    }
}
