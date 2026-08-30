package com.ninni.teallib.core.mixin;

import com.ninni.teallib.api.common.entity.catchable.CustomInventoryRendering;
import com.ninni.teallib.core.TealLib;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin implements CustomInventoryRendering {
    @Unique private boolean spawn$isRenderedInTooltip;

    @Override
    public boolean renderedInTooltip() {
        return spawn$isRenderedInTooltip;
    }

    @Override
    public void setRenderedInTooltip(boolean bl) {
        spawn$isRenderedInTooltip = bl;
    }



    @Inject(method = "getTypeName", at = @At("HEAD"), cancellable = true)
    private void replaceBabyName(CallbackInfoReturnable<Component> cir) {
        Entity self = (Entity) (Object) this;
        if (TealLib.CLIENT_CONFIG.replaceBabyMobNames.get()) {
            if (!(self instanceof LivingEntity mob) || !mob.isBaby()) return;

            String key = self.getType().getDescriptionId() + ".baby";
            MutableComponent baby = Component.translatable(key);

            if (baby.getString().equals(key)) {
                baby = Component.translatable("tooltip.teallib.default_baby");
                cir.setReturnValue(baby.append(self.getType().getDescription()));
            } else {
                cir.setReturnValue(baby);
            }
        }
    }
}
