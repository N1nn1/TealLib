package com.ninni.teallib.api.common.item.tooltip;

import net.minecraft.nbt.ListTag;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record CapturedMobsTooltipData(ListTag capturedMobs) implements TooltipComponent {
}