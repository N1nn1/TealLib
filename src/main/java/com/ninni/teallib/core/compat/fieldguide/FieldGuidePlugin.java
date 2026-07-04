package com.ninni.teallib.core.compat.fieldguide;

import com.evandev.fieldguide.variant.FieldGuideVariantManager;
import com.ninni.teallib.api.common.entity.variant.JsonVariantHolder;

public class FieldGuidePlugin {
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void register() {
        FieldGuideVariantManager.registerProvider((Class) JsonVariantHolder.class, new JsonVariantProvider());
    }
}
