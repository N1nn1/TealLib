package com.ninni.teallib.core.mixin;

import com.ninni.teallib.api.common.entity.variant.component.EntityVariantComponents;
import com.ninni.teallib.api.common.item.tooltip.TooltipUtils;
import com.ninni.teallib.core.TealLib;
import com.ninni.teallib.api.common.item.tooltip.CapturedMobsTooltipData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
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

    @Inject(method = "appendHoverText", at = @At("HEAD"), cancellable = true)
    private void S$appendHoverText(ItemStack stack, TooltipContext tooltipContext, List<Component> list, TooltipFlag flag, CallbackInfo ci) {
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(type);

        if (TealLib.CLIENT_CONFIG.globalVariantTooltip.get() && (key.getNamespace().equals("minecraft") || key.getNamespace().equals("spawn"))) {
            ci.cancel();
            CustomData customdata = stack.getOrDefault(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY);
            if (!customdata.isEmpty()) {
                EntityVariantComponents.addToTooltip(list, customdata.copyTag(), tooltipContext, key);
            }
        }
    }
}
