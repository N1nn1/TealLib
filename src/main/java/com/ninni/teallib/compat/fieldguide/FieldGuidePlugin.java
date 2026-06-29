package com.ninni.teallib.compat.fieldguide;

import com.evandev.fieldguide.variant.FieldGuideVariantManager;
import com.ninni.teallib.common.entity.variant.JsonVariantHolder;

public class FieldGuidePlugin {
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void register() {
        FieldGuideVariantManager.registerProvider((Class) JsonVariantHolder.class, new JsonVariantProvider());
    }
}
