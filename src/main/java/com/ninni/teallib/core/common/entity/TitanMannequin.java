package com.ninni.teallib.core.common.entity;

import com.ninni.teallib.api.common.entity.legged.Leg;
import com.ninni.teallib.api.common.entity.legged.ProceduralWalker;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class TitanMannequin extends PathfinderMob implements ProceduralWalker {

    public Vec3 bodyOffset = Vec3.ZERO;
    public Vec3 bodyOffseto = Vec3.ZERO;
    public Leg[] legs = new Leg[6];

    public TitanMannequin(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.initLegs();
        // create new look control that has a small max rot speed
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.FOLLOW_RANGE, 35.0D);
    }

    public Vec3 getBodyOffset() {
        return bodyOffset;
    }

    public Vec3 getBodyOffseto() {
        return bodyOffseto;
    }

    @Override
    public void initLegs() {
        legs[0] = new Leg(this, new Vec3(0.75, 0, 0.75), 0, 5);
        legs[1] = new Leg(this, new Vec3(-0.75, 0, 0.75), 1, 5);

        legs[2] = new Leg(this, new Vec3(0.9, 0, 0.0), 2, 5);
        legs[3] = new Leg(this, new Vec3(-0.9, 0, 0.0), 3, 5);

        legs[4] = new Leg(this, new Vec3(0.75, 0, -0.75), 4, 5);
        legs[5] = new Leg(this, new Vec3(-0.75, 0, -0.75), 5, 5);

    }

    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.SNIFFER_STEP, 0.15F, 1.0F);
    }

    protected SoundEvent getAmbientSound() {return SoundEvents.SNIFFER_IDLE;}

    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SNIFFER_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.SNIFFER_DEATH;
    }


    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override
    public Leg[] getLegs() {
        return legs;
    }

    @Override
    public LivingEntity getEntity() {
        return this;
    }

    @Override
    public void tick() {
        super.tick();
        setTargetLegPos();
        updateLegPos();
        this.bodyOffseto = this.bodyOffset;
        this.bodyOffset  = this.bodyOffset.lerp(this.getBodyPosition(), 0.8);
    }
}
