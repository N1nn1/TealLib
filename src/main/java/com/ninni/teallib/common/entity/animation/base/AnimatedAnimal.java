package com.ninni.teallib.common.entity.animation.base;

import com.ninni.teallib.common.entity.animation.EntityAnimationController;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public abstract class AnimatedAnimal extends Animal {
    private static final EntityDataAccessor<CompoundTag> ANIMATION_SYNC = SynchedEntityData.defineId(AnimatedAnimal.class, EntityDataSerializers.COMPOUND_TAG);
    protected final EntityAnimationController animations = new EntityAnimationController(this);

    protected AnimatedAnimal(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANIMATION_SYNC, new CompoundTag());
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            animations.tickClient();
        } else {
            animations.tickServer();
            if (animations.consumeDirty()) entityData.set(ANIMATION_SYNC, animations.writeSync());
        }
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (ANIMATION_SYNC.equals(key) && level().isClientSide) {
            animations.readSync(entityData.get(ANIMATION_SYNC));
        }
    }

    public EntityAnimationController animations() {
        return animations;
    }
}