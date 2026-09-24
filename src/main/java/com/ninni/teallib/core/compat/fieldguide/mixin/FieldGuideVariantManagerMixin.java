package com.ninni.teallib.core.compat.fieldguide.mixin;

import com.evandev.fieldguide.api.variant.VariantDef;
import com.evandev.fieldguide.variant.FieldGuideVariantManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FieldGuideVariantManager.class)
public class FieldGuideVariantManagerMixin {

    @Inject(method = "getVariantDisplayName", at = @At("HEAD"), cancellable = true)
    private static void teallib$getVariantDisplayName(VariantDef variant, CallbackInfoReturnable<Component> cir) {

        Component tealVariant = Component.empty();

        if (variant.value() instanceof ResourceLocation location && variant.id().startsWith("tealvariant/")) {
            String[] values = variant.id().replace("tealvariant/", "").split(":");
            if (values.length == 4) {
                tealVariant = Component.translatable("variant." + values[0] + "." + values[1] + "." + location.getNamespace() + "." + location.getPath());
            }
        }

        if (!tealVariant.getString().isBlank()) {
            cir.setReturnValue(tealVariant);
        }
    }
}
