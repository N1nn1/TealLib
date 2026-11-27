package com.ninni.teallib.api.common.entity.legged;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public interface ProceduralWalker {
    float bodyTilt = 0;

    default void initLegs() {

    }

    default List<Leg> getLegs() {
        return null;
    }

    default LivingEntity getEntity() {
        return null;
    }

    default float moveThreshold() {
        return 0.5f;
    }

    default void updateLegPos() {
        for (int i = 0; i < getLegs().size(); i++) {
            Leg leg = getLegs().get(i);
            leg.tick();

            if (leg.moving) continue;

            boolean adj1Moving = getLegs().get((i + 1) % 4).moving;
            boolean adj2Moving = getLegs().get((i + 2) % 4).moving;

            if (adj1Moving || adj2Moving) continue;

            float threshold = this.isMoving() ? moveThreshold() : 0.1F;
            leg.tryMoveToNewTarget(threshold, getEntity());
        }
    }

    default void playLegStepSound(BlockPos pos, BlockState state) {
    }

    private boolean isMoving() {
        double d0 = getEntity().getX() - getEntity().xo;
        double d1 = getEntity().getZ() - getEntity().zo;

        double r0 = getEntity().yBodyRot - getEntity().yBodyRotO;
        //stole this number from a forum. I'm sorry I can't explain what it means it has to do with floats and small numbers
        return d0 * d0 + d1 * d1 > 2.500000277905201E-7 || r0 != 0;
    }

    default void setTargetLegPos(){
        for (Leg leg : getLegs()) {
            leg.targetPos = leg.updatePosition();
        }
    }

    default Vec3 getBodyOffset() {
        return null;
    }

    default float getBodyTilt() {
        return bodyTilt;
    }

    default Vec3 getAverageLegPosition() {
        Vec3 momentum = getEntity().getDeltaMovement();
        Vec3 balancePoint = new Vec3(0,1,0);

        for (int i = 0; i < 6; i++) {
            Leg leg = getLegs().get(i);
            Vec3 legOffset = leg.getCurrentPos().subtract(getEntity().position());
            balancePoint = balancePoint.add(legOffset.scale(leg.weight));
        }

        return balancePoint.add(momentum.scale(0.5f));
    }

    default Vec3 getBodyPosition() {
        return getAverageLegPosition();
    }

    default float getBodyRoll() {
        double leftY = 0, rightY = 0;
        int leftCount = 0, rightCount = 0;

        for (Leg leg : getLegs()) {
            if (leg != null && leg.getCurrentPos() != null) {
                double y = leg.getCurrentPos().y;
                if (leg.getIndex() % 2 == 0) {
                    leftY += y;
                    leftCount++;
                } else if (leg.getIndex() % 2 != 0) {
                    rightY += y;
                    rightCount++;
                }
            }
        }

        if (leftCount == 0 || rightCount == 0) return 0;

        double avgLeftY = leftY / leftCount;
        double avgRightY = rightY / rightCount;

        return (float) (avgLeftY - avgRightY);
    }

    default float getBodyPitch() {
        double frontY = 0, backY = 0;
        int frontCount = 0, backCount = 0;

        for (Leg leg : getLegs()) {
            if (leg != null && leg.getCurrentPos() != null) {
                double y = leg.getCurrentPos().y;
                if (leg.getIndex() < 2) {
                    frontY += y;
                    frontCount++;
                } else if (leg.getIndex() > 1) {
                    backY += y;
                    backCount++;
                }
            }
        }

        if (frontCount == 0 || backCount == 0) return 0;

        double avgFrontY = frontY / frontCount;
        double avgBackY = backY / backCount;

        return (float) (avgBackY - avgFrontY);
    }
}
