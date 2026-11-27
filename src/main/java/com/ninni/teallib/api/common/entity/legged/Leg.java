package com.ninni.teallib.api.common.entity.legged;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class Leg {

    Vec3 restingOffset;
    Vec3 currentPos;
    private Vec3 prevPos;
    Vec3 startPos;
    Vec3 targetPos;
    LivingEntity entity;
    boolean wasAtTarget = true;

    int moveDuration;
    int moveTicks = 0;

    boolean moving = false;
    int index;

    public Leg(LivingEntity entity, Vec3 restingOffset, int i, int dir) {
        this.entity = entity;
        this.restingOffset = restingOffset;
        this.currentPos = updatePosition();
        this.targetPos = this.currentPos;
        this.startPos = this.currentPos;
        this.index = i;
        this.moveDuration = dir;
    }

    public Vec3 updatePosition() {
        Vec3 rotatedOffset = restingOffset.yRot(-entity.yBodyRot * ((float)Math.PI / 180F));
        Vec3 mobCenter = entity.position();

        Vec3 startPos = mobCenter.add(rotatedOffset).add(0, 2.0, 0);

        BlockHitResult hit = entity.level().clip(new ClipContext(
                startPos,
                startPos.subtract(0, 6.0, 0),
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                entity
        ));

        if (hit.getType() == HitResult.Type.BLOCK) {
            return hit.getLocation();
        }

        Vec3 pos = mobCenter.add(rotatedOffset);
        BlockPos groundPos = BlockPos.containing(pos).below();
        return Vec3.atBottomCenterOf(groundPos.above());
    }

    public void tick() {
        prevPos = currentPos;
        if (moving) {

            moveTicks++;
            if (moveTicks >= moveDuration) {
                moving = false;
                currentPos = targetPos;
            } else {
                float progress = getProgress();
                currentPos = lerp(startPos, targetPos, progress);
            }
        }
    }


    public Vec3 lerp(Vec3 from, Vec3 to, double delta) {
        if (delta < 0.5)
            return new Vec3(Mth.lerp(delta, from.x, to.x), Mth.lerp(delta, from.y, to.y+0.3f), Mth.lerp(delta, from.z, to.z));
        return new Vec3(Mth.lerp(delta, from.x, to.x), Mth.lerp(delta, from.y+0.3f, to.y), Mth.lerp(delta, from.z, to.z));
    }

    public void tryMoveToNewTarget(float threshold, LivingEntity entity) {
        Vec3 newTarget = updatePosition();
        boolean atTarget = currentPos.distanceTo(newTarget) <= threshold;

        if (!atTarget) {
            this.startPos = currentPos;
            this.targetPos = newTarget;
            this.moveTicks = 0;
            this.moving = true;
            this.wasAtTarget = false;
        } else {
            if (!this.wasAtTarget) {
                entity.playSound(SoundEvents.BAMBOO_WOOD_BREAK);
            }
            this.wasAtTarget = true;
            this.moving = false;
        }
    }

    public Vec3 getTargetPos() {
        return targetPos;
    }

    public Vec3 getCurrentPos() {
        return currentPos;
    }
    public Vec3 getPrevPos() {
        return prevPos;
    }

    public float getProgress() {
        if (!moving || moveDuration <= 0) return 1.0f;
        return Mth.clamp((float) moveTicks / moveDuration, 0.0f, 1.0f);
    }

    public int getIndex() {
        return index;
    }

    public Vec3 getRestingOffset() {
        return restingOffset;
    }
}
