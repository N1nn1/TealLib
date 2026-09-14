package com.ninni.teallib.core.mixin.client;

import com.ninni.teallib.core.TealLib;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(TranslatableContents.class)
public abstract class TranslatableContentsMixin {
    @Shadow @Final private String key;

    @ModifyArg(method = "decompose", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/contents/TranslatableContents;decomposeTemplate(Ljava/lang/String;Ljava/util/function/Consumer;)V"), index = 0)
    private String teallib$babyName(String template) {
        if (!key.equals(TealLib.BABY_NAME)) return template;
        return TealLib.replaceBabyMobNames() ? "%1$s" : "%2$s";
    }
}
