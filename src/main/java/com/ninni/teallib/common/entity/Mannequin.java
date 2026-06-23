package com.ninni.teallib.common.entity;

import com.ninni.teallib.TealLib;
import com.ninni.teallib.common.data.entityvariant.EntityVariantManager;
import com.ninni.teallib.common.entity.animation.base.AnimatedLivingEntity;
import com.ninni.teallib.common.entity.variant.JsonVariantHolder;
import com.ninni.teallib.common.entity.variant.base.AbstractVariantAgeableMob;
import com.ninni.teallib.registry.TealItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

public class Mannequin extends AnimatedLivingEntity implements JsonVariantHolder {
    private static final Predicate<Entity> RIDABLE_MINECARTS = p_31582_ -> p_31582_ instanceof AbstractMinecart && ((AbstractMinecart)p_31582_).canBeRidden();
    private static final EntityDataAccessor<Boolean> SITTING_DATA = SynchedEntityData.defineId(Mannequin.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_ROTATION = SynchedEntityData.defineId(Mannequin.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_TARGET_ROTATION = SynchedEntityData.defineId(Mannequin.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<String> DATA_VARIANT = SynchedEntityData.defineId(Mannequin.class, EntityDataSerializers.STRING);

    public static ResourceLocation PUNCH = ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "punch");
    public static ResourceLocation ROTATION = ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "rotation");
    public static ResourceLocation SIT = ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "sit");
    public static ResourceLocation SITTING = ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "sitting");
    public static ResourceLocation STAND_UP = ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "stand_up");

    public float prevRotation;

    public Mannequin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createLivingAttributes().add(Attributes.STEP_HEIGHT, 0.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SITTING_DATA, false);
        builder.define(DATA_ROTATION, 0f);
        builder.define(DATA_TARGET_ROTATION, 0f);
        builder.define(DATA_VARIANT, this.getDefaultVariant().toString());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Sitting", isSitting());
        compound.putFloat("Rotation", getRotation());
        compound.putFloat("YHeadRot", getYHeadRot());
        compound.putString("Variant", this.getVariant().toString());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setSitting(compound.getBoolean("Sitting"));
        this.setRotation(compound.getFloat("Rotation"));
        this.setYHeadRot(compound.getFloat("YHeadRot"));
        this.loadOrAssignVariant(this, compound, "Variant");
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    protected void pushEntities() {
        for (Entity entity : this.level().getEntities(this, this.getBoundingBox(), RIDABLE_MINECARTS)) {
            if (this.distanceToSqr(entity) <= 0.2) {
                entity.push(this);
            }
        }
    }
    @Override
    public InteractionResult interactAt(Player player, Vec3 vec, InteractionHand hand) {
        if (this.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (player.isShiftKeyDown()) {
            if (this.animations.isStopped(Mannequin.SIT) && this.animations.isStopped(Mannequin.STAND_UP)) {
                boolean value = !isSitting();
                if (value) {
                    this.animations.playOnce(SIT, 20);
                    this.animations.playLoop(SITTING);
                } else {
                    this.animations.playOnce(STAND_UP, 20);
                    this.animations.stop(SITTING);
                }
                setSitting(value);
            }
        } else {
            float amount = player.getItemInHand(hand).isEmpty() ? -0.2F : 0.2F;
            this.setTargetRotation(Mth.clamp(getRotation() + amount, 0.0F, 1.0F));
        }

        return InteractionResult.CONSUME;
    }


    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide && source.getEntity() instanceof LivingEntity living) {
            if (living.isShiftKeyDown()) {
                if (living instanceof Player player && !player.isCreative() || !(living instanceof Player))  {
                    this.spawnAtLocation(TealItems.MANNEQUIN.get());
                }
                this.kill();
            }
            else this.animations.playOnce(PUNCH, 50);
        }
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            if (this.isSitting() && animations.isStopped(SITTING)) {
                animations.playLoop(SITTING);
            }
            float lerp = Mth.lerp(0.25f, getRotation(), getTargetRotation());
            setRotation(lerp);
            animations.setProgress(ROTATION, lerp);
        } else {
            prevRotation = getRotation();
        }
    }

    public float getTargetRotation() {
        return this.entityData.get(DATA_TARGET_ROTATION);
    }
    public void setTargetRotation(float rotation) {
        this.entityData.set(DATA_TARGET_ROTATION, rotation);
    }

    public float getRotation() {
        return this.entityData.get(DATA_ROTATION);
    }
    public void setRotation(float rotation) {
        this.entityData.set(DATA_ROTATION, rotation);
    }

    public boolean isSitting() {
        return this.entityData.get(SITTING_DATA);
    }
    public void setSitting(boolean value) {
        this.entityData.set(SITTING_DATA, value);
    }

    @Override
    public void setVariant(ResourceLocation resourceLocation) {
        this.entityData.set(DATA_VARIANT, resourceLocation.toString());
    }
    @Override
    public ResourceLocation getVariant() {
        return ResourceLocation.parse(this.entityData.get(DATA_VARIANT));
    }

    @Override
    public void kill() {
        this.remove(Entity.RemovalReason.KILLED);
        this.gameEvent(GameEvent.ENTITY_DIE);
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(TealItems.MANNEQUIN);
    }

    @Override
    public boolean attackable() {
        return false;
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return List.of();
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
    }

    @Override
    public HumanoidArm getMainArm() {
        return null;
    }

    @Override
    public ResourceLocation getDefaultVariant() {
        return ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "teal");
    }
}
