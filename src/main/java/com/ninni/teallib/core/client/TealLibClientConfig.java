package com.ninni.teallib.core.client;

import net.neoforged.neoforge.common.ModConfigSpec;

public class TealLibClientConfig {
    public final ModConfigSpec.BooleanValue bucketTooltip;
    public final ModConfigSpec.BooleanValue axolotlVariantTooltip;
    public final ModConfigSpec.BooleanValue replaceBabyMobNames;

    public TealLibClientConfig(final ModConfigSpec.Builder builder) {

        builder.translation("config.teallib.section.vanilla_changes").push("vanilla_changes");
        bucketTooltip = builder
                .comment("config.teallib.bucket_tooltip.tooltip")
                .translation("config.teallib.bucket_tooltip")
                .define("bucket_tooltip", true);
        axolotlVariantTooltip = builder
                .comment("config.teallib.axolotl_bucket_tooltip.tooltip")
                .translation("config.teallib.axolotl_bucket_tooltip")
                .define("axolotl_bucket_tooltip", true);
        replaceBabyMobNames = builder
                .comment("config.spawn.replace_baby_mob_names.tooltip")
                .translation("config.spawn.replace_baby_mob_names")
                .define("replace_baby_mob_names", true);
        builder.pop();
    }
}
