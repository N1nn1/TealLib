package com.ninni.teallib.api.common.entity;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

import java.util.List;

public interface Fossorial {
    boolean isBuried();

    static boolean isEmptySpot(Level level, Mob mob) {
        List<Mob> mobs = level.getEntitiesOfClass(Mob.class, mob.getBoundingBox(), lilGuy -> lilGuy instanceof Fossorial fossorial && !lilGuy.is(mob) && fossorial.isBuried());
        return mobs.isEmpty();
    }
}
