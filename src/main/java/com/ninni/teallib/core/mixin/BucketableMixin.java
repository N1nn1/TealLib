package com.ninni.teallib.core.mixin;

import com.ninni.teallib.api.common.data.variant.util.VariantAttachments;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Bucketable.class)
public interface BucketableMixin {

    @Inject(method = "saveDefaultDataToBucketTag", at = @At("TAIL"))
    private static void S$saveDefaultDataToBucketTag(Mob mob, ItemStack stack, CallbackInfo ci) {
        CustomData.update(DataComponents.BUCKET_ENTITY_DATA, stack, tag -> {
            VariantAttachments.toTag(mob, tag);
        });
    }

    @Inject(method = "loadDefaultDataFromBucketTag", at = @At("TAIL"))
    private static void S$loadDefaultDataFromBucketTag(Mob mob, CompoundTag tag, CallbackInfo ci) {
        VariantAttachments.fromTag(mob, tag, true);
    }
}
