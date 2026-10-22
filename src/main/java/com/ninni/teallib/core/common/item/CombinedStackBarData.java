package com.ninni.teallib.core.common.item;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record CombinedStackBarData(int index, int max) implements TooltipComponent {
}