package com.ninni.teallib.common.entity.variant.base;

import com.ninni.teallib.common.data.entityvariant.EntityVariantManager;
import com.ninni.teallib.common.entity.variant.JsonVariantHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.AbstractSchoolingFish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractVariantSchoolingFish extends AbstractSchoolingFish implements JsonVariantHolder {
    private static final EntityDataAccessor<String> DATA_VARIANT = SynchedEntityData.defineId(AbstractVariantSchoolingFish.class, EntityDataSerializers.STRING);

    public AbstractVariantSchoolingFish(EntityType<? extends AbstractSchoolingFish> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor serverLevelAccessor, @NotNull DifficultyInstance difficultyInstance, @NotNull MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        if (mobSpawnType != MobSpawnType.BUCKET) EntityVariantManager.getNaturallyOccurringVariant(this);
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    //region DATA
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, this.getDefaultVariant().toString());
    }

    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("Variant", this.getVariant().toString());
    }

    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.loadOrAssignVariant(this, tag, "Variant");
    }
    public void saveToBucketTag(@NotNull ItemStack itemStack) {
        super.saveToBucketTag(itemStack);
        CustomData.update(DataComponents.BUCKET_ENTITY_DATA, itemStack, (tag) -> {
            tag.putString("Variant", this.getVariant().toString());
        });
    }

    @Override
    public void loadFromBucketTag(@NotNull CompoundTag tag) {
        super.loadFromBucketTag(tag);
        this.loadOrAssignVariant(this, tag, "Variant");
    }

    @Override
    public void setVariant(ResourceLocation resourceLocation) {
        this.entityData.set(DATA_VARIANT, resourceLocation.toString());
    }

    @Override
    public ResourceLocation getVariant() {
        return ResourceLocation.parse(this.entityData.get(DATA_VARIANT));
    }
    //endregion

}
