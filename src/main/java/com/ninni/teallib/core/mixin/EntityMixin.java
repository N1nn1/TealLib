package com.ninni.teallib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.ninni.teallib.api.common.entity.catchable.CustomInventoryRendering;
import com.ninni.teallib.core.TealLib;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

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



    @ModifyExpressionValue(method = "getName", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getTypeName()Lnet/minecraft/network/chat/Component;"))
    private Component replaceBabyName(Component typeName) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof LivingEntity mob) || !mob.isBaby()) return typeName;

        String key = self.getType().getDescriptionId() + ".baby";
        MutableComponent baby = Component.translatable(key);

        if (baby.getString().equals(key)) {
            baby = Component.translatable("tooltip.teallib.default_baby");
            return TealLib.babyName(baby.append(typeName), typeName);
        } else {
            return TealLib.babyName(baby, typeName);
        }
    }
}
