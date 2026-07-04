package com.ninni.teallib.api.common.item;

import com.ninni.teallib.core.TealLib;
import com.ninni.teallib.api.common.data.entityvariant.EntityVariantManager;
import com.ninni.teallib.api.common.entity.catchable.Catchable;
import com.ninni.teallib.api.common.item.tooltip.CapturedMobsTooltipData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * An Item that stores and releases {@link com.ninni.teallib.api.common.entity.catchable.Catchable Catchable Mobs}.
 * <p>
 * This item behaves similarly to a bucket: entities can be captured into it,
 * serialized into NBT, and later released back into the world with their
 * stored state preserved.
 * <p>
 * It also supports dispenser behavior, custom remainder items, and optional
 * variant-aware tooltips when multiple variants exist for the stored entity type.
 */
public class CatchableMobBucketItem extends Item {
    public final Supplier<? extends EntityType<?>> entityTypeSupplier;
    public final @Nullable Item remainderItem;
    public static final DispenseItemBehavior DISPENSE_ITEM_BEHAVIOR = new DefaultDispenseItemBehavior() {
        public @NotNull ItemStack execute(BlockSource blockSource, ItemStack stack) {
            CatchableMobBucketItem item = (CatchableMobBucketItem)stack.getItem();
            BlockPos blockPos = blockSource.pos().relative(blockSource.state().getValue(DispenserBlock.FACING));
            Level level = blockSource.level();

            if (level instanceof ServerLevel serverLevel) {
                Vec3 vec = blockPos.getCenter().add(0, -0.5, 0);
                CompoundTag tag = stack.getOrDefault(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY).copyTag();
                Catchable.releaseFromTag(tag, serverLevel, vec,0,0, item.entityTypeSupplier.get());
                level.gameEvent(null, GameEvent.ENTITY_PLACE, blockPos);

                if (tag.contains("Item")) {
                    return ItemStack.parseOptional(level.registryAccess(), tag.getCompound("Item"));
                }
            }
            return ItemStack.EMPTY;
        }
    };

    public CatchableMobBucketItem(Properties properties, Supplier<? extends EntityType<?>> entityTypeSupplier) {
        this(properties, entityTypeSupplier, null);
    }

    public CatchableMobBucketItem(Properties properties, Supplier<? extends EntityType<?>> entityTypeSupplier, @Nullable Item remainderItem) {
        super(properties);
        this.entityTypeSupplier = entityTypeSupplier;
        this.remainderItem = remainderItem;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand interactionHand) {
        BlockHitResult blockHitResult = BucketItem.getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        ItemStack itemStack = player.getItemInHand(interactionHand);
        Direction direction = blockHitResult.getDirection();
        BlockPos blockPos = blockHitResult.getBlockPos();
        BlockPos blockPos3 = blockPos.relative(direction);

        if (blockHitResult.getType() == HitResult.Type.BLOCK) {
            this.spawn(player, interactionHand, level, itemStack, blockPos3);
            level.gameEvent(player, GameEvent.ENTITY_PLACE, blockPos);
            player.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResultHolder.success(itemStack);
        }
        return super.use(level, player, interactionHand);
    }

    private void spawn(@Nullable Player player, InteractionHand interactionHand, Level level, ItemStack stack, BlockPos blockPos) {
        if (level instanceof ServerLevel serverLevel) {
            Vec3 vec = blockPos.getCenter().add(0, -0.5, 0);
            CompoundTag tag = stack.getOrDefault(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY).copyTag();
            Catchable.releaseFromTag(tag, serverLevel, vec,player.getYRot() - 180,0, entityTypeSupplier.get());
            this.giveItemBack(player, interactionHand, stack, level.registryAccess());
        }
    }

    public void giveItemBack(Player player, InteractionHand interactionHand, ItemStack stack, RegistryAccess registryAccess) {
        CompoundTag tag = stack.getOrDefault(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY).copyTag();
        ItemStack remainder = null;

        if (!player.isCreative()) {
            if (this.remainderItem != null) remainder = this.remainderItem.getDefaultInstance();
            if (tag.contains("Item")) remainder = ItemStack.parseOptional(registryAccess, tag.getCompound("Item"));
            if (remainder != null) {
                if (stack.getCount() == 1) player.setItemInHand(interactionHand, remainder);
                else {
                    stack.shrink(1);
                    if (!player.addItem(remainder)) player.drop(remainder, true);
                }
            }
        }
    }

    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack stack) {
        if (TealLib.CLIENT_CONFIG.bucketTooltip.get()) {
            CompoundTag stackTag = stack.getOrDefault(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY).copyTag();
            ListTag capturedMobs = new ListTag();
            CompoundTag tag = stackTag.isEmpty() ? new CompoundTag() : stackTag.copy();
            tag.putString("id", EntityType.getKey(entityTypeSupplier.get()).toString());

            capturedMobs.add(tag);
            return Optional.of(new CapturedMobsTooltipData(capturedMobs));
        }
        return super.getTooltipImage(stack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> list, @NotNull TooltipFlag tooltipFlag) {
        CompoundTag compoundTag = stack.getOrDefault(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY).copyTag();

        if (!compoundTag.isEmpty() && compoundTag.contains("CustomName") && !TealLib.CLIENT_CONFIG.bucketTooltip.get()) {
            list.add(Component.literal(compoundTag.getString("CustomName")).withStyle(Style.EMPTY.withItalic(true).withColor(ChatFormatting.YELLOW)));
        }

        if (context.level() == null) return;

        if (EntityVariantManager.getVariantCountFor(context.level().registryAccess(), entityTypeSupplier.get()) > 1) {
            if (!compoundTag.isEmpty() && compoundTag.contains("Variant")) {
                ResourceLocation loc = ResourceLocation.parse(compoundTag.getString("Variant"));
                String name = BuiltInRegistries.ENTITY_TYPE.getKey(entityTypeSupplier.get()).getPath();
                list.add(Component.translatable("variant." + loc.getNamespace() + "."+ name +"." + loc.getPath()).withStyle(Style.EMPTY.withItalic(true).withColor(ChatFormatting.GRAY)));
            }
        }

        super.appendHoverText(stack, context, list, tooltipFlag);
    }
}
