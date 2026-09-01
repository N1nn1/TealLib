package com.ninni.teallib.core.client;

import net.neoforged.neoforge.common.ModConfigSpec;

public class TealLibClientConfig {
    public final ModConfigSpec.BooleanValue bucketTooltip;
    public final ModConfigSpec.BooleanValue globalVariantTooltip;
    public final ModConfigSpec.BooleanValue replaceBabyMobNames;

    public TealLibClientConfig(final ModConfigSpec.Builder builder) {

        builder.translation("config.teallib.section.vanilla_changes").push("vanilla_changes");
        bucketTooltip = builder
                .comment("""
                        Whether buckets and other entity containing items render their entity in the tooltip.
                        Default: true
                        """)
                .translation("config.teallib.bucket_tooltip")
                .define("bucket_tooltip", true);
        globalVariantTooltip = builder
                .comment("""
                        Whether the vanilla bucket variant tooltips for mobs are replaced by Teal Lib's system.
                        Effectively this just makes Tropical Fish tooltips colorful, and adds Axolotl variant tooltips
                        Default: true
                        """)
                .translation("config.teallib.global_bucket_tooltip")
                .define("global_bucket_tooltip", true);
        replaceBabyMobNames = builder
                .comment("""
                        Whether baby mob names are replaced by custom ones (Ex: Puppy for Wolves or Panda Cub for Pandas).
                        Default: true
                        """)
                .translation("config.spawn.replace_baby_mob_names")
                .define("replace_baby_mob_names", true);
        builder.pop();
    }
}
