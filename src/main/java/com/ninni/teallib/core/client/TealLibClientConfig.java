package com.ninni.teallib.core.client;

import net.neoforged.neoforge.common.ModConfigSpec;

public class TealLibClientConfig {
    public final ModConfigSpec.BooleanValue bucketTooltip;

    public TealLibClientConfig(final ModConfigSpec.Builder builder) {

        builder.translation("config.teallib.section.vanilla_changes").push("vanilla_changes");
        bucketTooltip = builder
                .comment("config.teallib.bucket_tooltip.tooltip")
                .translation("config.teallib.bucket_tooltip")
                .define("bucket_tooltip", true);
        builder.pop();
    }
}
