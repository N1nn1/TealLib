package com.ninni.teallib.api.common.entity;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

public interface CustomDamageType {
    ResourceKey<DamageType> getDamageType();
}
