package com.ninni.teallib.core.registry;

import com.ninni.teallib.core.TealLib;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class TealParticleType {

    public static final DeferredRegister<ParticleType<?>> DEF_REG = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, TealLib.MODID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> STUN = register("stun");

    private static DeferredHolder<ParticleType<?>, SimpleParticleType> register(String id) {
        return DEF_REG.register(id, () -> new SimpleParticleType(false));
    }
}
