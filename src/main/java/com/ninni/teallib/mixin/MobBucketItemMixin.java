package com.ninni.teallib.mixin;

import com.ninni.teallib.TealLib;
import com.ninni.teallib.common.item.tooltip.CapturedMobsTooltipData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(value = MobBucketItem.class, priority = 676767)
public class MobBucketItemMixin extends BucketItem {
    @Shadow @Final private EntityType<?> type;

    public MobBucketItemMixin(Fluid fluid, Properties builder) {
        super(fluid, builder);
    }


    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack stack) {
        if (TealLib.CLIENT_CONFIG.bucketTooltip.get()) {
            CompoundTag stackTag = stack.getOrDefault(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY).copyTag();
            if (type == null) return super.getTooltipImage(stack);
            ListTag capturedMobs = new ListTag();
            CompoundTag tag = stackTag.isEmpty() ? new CompoundTag() : stackTag.copy();
            tag.putString("id", EntityType.getKey(type).toString());

            capturedMobs.add(tag);
            return Optional.of(new CapturedMobsTooltipData(capturedMobs));
        }
        return super.getTooltipImage(stack);
    }
}
