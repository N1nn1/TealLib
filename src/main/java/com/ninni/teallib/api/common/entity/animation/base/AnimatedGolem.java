package com.ninni.teallib.api.common.entity.animation.base;

import com.ninni.teallib.api.common.entity.animation.EntityAnimationController;
import com.ninni.teallib.api.common.entity.animation.EntityAnimationHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * A class that implements the basics of an {@link EntityAnimationController EntityAnimationController} system extending {@link net.minecraft.world.entity.animal.AbstractGolem AbstractGolem}
 */
public abstract class AnimatedGolem extends AbstractGolem implements EntityAnimationHolder {
    private static final EntityDataAccessor<CompoundTag> ANIMATION_SYNC = SynchedEntityData.defineId(AnimatedGolem.class, EntityDataSerializers.COMPOUND_TAG);
    protected final EntityAnimationController animations = new EntityAnimationController(this);

    protected AnimatedGolem(EntityType<? extends AbstractGolem> entityType, Level level) {
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