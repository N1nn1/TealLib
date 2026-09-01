package com.ninni.teallib.core.compat.fieldguide;

import com.evandev.fieldguide.variant.FieldGuideVariantManager;
import net.minecraft.world.entity.Mob;

public class FieldGuidePlugin {
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void register() {
        FieldGuideVariantManager.registerProvider(Mob.class, new JsonVariantProvider());
    }
}
