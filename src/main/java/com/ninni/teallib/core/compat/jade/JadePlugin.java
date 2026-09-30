package com.ninni.teallib.core.compat.jade;

import com.ninni.teallib.core.compat.jade.component.VariantComponentProvider;
import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class JadePlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(new VariantComponentProvider(), LivingEntity.class);
    }
}
