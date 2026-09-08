package com.ninni.teallib.api.common.entity;

import com.ninni.teallib.core.registry.TealParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;

public class EntityUtils {

    public static void spawnStunParticles(Mob mob, int amount, float xzOffset, float yOffset) {
        if (mob.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(TealParticleType.STUN.get(), mob.position().x, mob.position().y + mob.getEyeHeight(), mob.position().z, amount, xzOffset, yOffset,xzOffset, 0);
        }
    }
}
