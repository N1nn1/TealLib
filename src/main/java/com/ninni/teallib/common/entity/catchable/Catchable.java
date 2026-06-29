package com.ninni.teallib.common.entity.catchable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.Vec3;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;
import java.util.Optional;

public interface Catchable {

    default boolean canBeCaught(@Nullable ItemStack stack, @Nullable Player player, @Nullable InteractionHand hand) {
        return true;
    };

    @Nullable
    default ItemStack getCaughtItem(ItemStack capturingItem) {
        return ItemStack.EMPTY;
    }

    @Nullable
    default Component cantBeCaughtReason(ItemStack capturingItem) {
        return null;
    }

    boolean hasBeenCaught();

    void setHasBeenCaught(boolean bl);

    SoundEvent getPickupSound(@Nullable ItemStack stack);
    SoundEvent getReleaseSound();

    default void saveDataToTag(Mob mob, CompoundTag tag) {
        saveDefaultDataToTag(mob, tag);
    }

    default void loadDataFromTag(Mob mob, CompoundTag tag) {
        loadDefaultDataFromTag(mob, tag);
    }

    static void saveDefaultDataToTag(Mob mob, CompoundTag tag) {
        if (mob.hasCustomName()) tag.putString("CustomName", Objects.requireNonNull(mob.getCustomName()).getString());
        if (mob.isNoAi()) tag.putBoolean("NoAI", mob.isNoAi());
        if (mob.isSilent()) tag.putBoolean("Silent", mob.isSilent());
        if (mob.isNoGravity()) tag.putBoolean("NoGravity", mob.isNoGravity());
        if (mob.hasGlowingTag()) tag.putBoolean("Glowing", true);
        if (mob.isInvulnerable()) tag.putBoolean("Invulnerable", mob.isInvulnerable());
        tag.putString("id", Objects.requireNonNull(mob.self().getEncodeId()));
        tag.putFloat("Health", mob.getHealth());
    }

    static void loadDefaultDataFromTag(Mob mob, CompoundTag tag) {
        if (tag.contains("CustomName")) mob.setCustomName(Component.literal(tag.getString("CustomName")));
        if (tag.contains("NoAI")) mob.setNoAi(tag.getBoolean("NoAI"));
        if (tag.contains("Silent")) mob.setSilent(tag.getBoolean("Silent"));
        if (tag.contains("NoGravity")) mob.setNoGravity(tag.getBoolean("NoGravity"));
        if (tag.contains("Glowing")) mob.setGlowingTag(tag.getBoolean("Glowing"));
        if (tag.contains("Invulnerable")) mob.setInvulnerable(tag.getBoolean("Invulnerable"));
        if (tag.contains("Health")) mob.setHealth(tag.getFloat("Health"));
    }

    static <T extends Mob & Catchable> Optional<InteractionResult> catchWithItem(Player player, InteractionHand hand, T mob, @Nullable CompoundTag extraData) {
        ItemStack stack = player.getItemInHand(hand);
        ItemStack captureStack = singleItemCopy(stack);

        if (!mob.isAlive()) return Optional.empty();

        if (mob.canBeCaught(stack, player, hand)) {
            if (mob.level().isClientSide) return Optional.of(InteractionResult.SUCCESS);
            mob.playSound(mob.getPickupSound(stack), 1.0F, 1.0F);

            ItemStack caughtItem = mob.getCaughtItem(captureStack);
            if (caughtItem == null) caughtItem = ItemStack.EMPTY;

            if (caughtItem.isEmpty()) {
                if (!stack.isEmpty()) {
                    CustomData.update(DataComponents.BUCKET_ENTITY_DATA, stack, tag -> {
                        mob.saveDataToTag(mob, tag);
                        if (extraData != null) tag.merge(extraData);
                    });
                    mob.discard();
                }
            } else {
                CustomData.update(DataComponents.BUCKET_ENTITY_DATA, caughtItem, tag -> {
                    mob.saveDataToTag(mob, tag);
                    if (extraData != null) tag.merge(extraData);
                });

                if (stack.getCount() == 1 && !player.isCreative()) {
                    player.setItemInHand(hand, caughtItem);
                } else {
                    if (!player.addItem(caughtItem)) {
                        player.drop(caughtItem, true);
                    }
                    if (!player.isCreative()) {
                        stack.shrink(1);
                    }
                }

                mob.discard();
            }

            return Optional.of(InteractionResult.SUCCESS);
        } else {
            if (mob.cantBeCaughtReason(captureStack) != null) {
                player.displayClientMessage(mob.cantBeCaughtReason(captureStack), true);
                return Optional.of(InteractionResult.SUCCESS);
            }
        }

        return Optional.empty();
    }

    @SuppressWarnings({"deprecation", "OverrideOnly"})
    static Mob releaseFromTag(CompoundTag tag, ServerLevel level, Vec3 vec, float yRot, float xRot, @Nullable EntityType<?> fallbackType) {

        if (tag != null && tag.contains("id")) {
            EntityType<?> type = EntityType.byString(tag.getString("id")).orElse(null);

            if (type != null) {
                Entity entity = type.create(level);
                if (!(entity instanceof Mob mob)) return null;

                mob.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.containing(vec)), MobSpawnType.BUCKET, null);

                mob.moveTo(vec.x, vec.y, vec.z, yRot, xRot);

                if (mob instanceof Catchable catchable) {
                    catchable.loadDataFromTag(mob, tag);
                    catchable.setHasBeenCaught(true);
                    level.playSound(null, BlockPos.containing(vec), catchable.getReleaseSound(), mob.getSoundSource(), 1, 1);
                }

                level.addFreshEntity(mob);

                if (mob instanceof Catchable catchable) mob.playSound(catchable.getReleaseSound(), 1, 1);

                return mob;
            }
        }

        Entity entity = Objects.requireNonNull(fallbackType).create(level);
        if (!(entity instanceof Mob mob)) return null;

        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.containing(vec)), MobSpawnType.MOB_SUMMONED, null);
        mob.moveTo(vec.x, vec.y, vec.z, yRot, xRot);

        if (mob instanceof Catchable catchable) catchable.setHasBeenCaught(true);

        level.addFreshEntity(mob);

        if (mob instanceof Catchable catchable) mob.playSound(catchable.getReleaseSound(), 1, 1);
        return mob;
    }

    static ItemStack singleItemCopy(ItemStack stack) {
        return stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1);
    }
}
