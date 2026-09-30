package com.ninni.teallib.core.compat.jade;

import com.ninni.teallib.core.compat.jade.component.BlockVariantComponentProvider;
import com.ninni.teallib.core.compat.jade.component.EntityVariantComponentProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.BaseEntityBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class JadePlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new BlockVariantComponentProvider(), BaseEntityBlock.class);
        registration.registerEntityComponent(new EntityVariantComponentProvider(), LivingEntity.class);
    }
}
