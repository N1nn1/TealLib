package com.ninni.teallib.api.client.renderer.item;

import com.ninni.teallib.core.TealLib;
import com.ninni.teallib.core.common.item.CombinedStackBarData;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;

public class CombinedStackBarRenderer implements ClientTooltipComponent {
    public static final ResourceLocation DOT = ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "container/combined_stack/dot");
    public static final ResourceLocation FILLED_DOT = ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "container/combined_stack/filled_dot");

    private final CombinedStackBarData data;

    public CombinedStackBarRenderer(CombinedStackBarData data) {
        this.data = data;
    }

    @Override
    public int getHeight() {
        return 12 + 2;
    }

    @Override
    public int getWidth(Font font) {
        return 80;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {

        for (int i = 0; i < data.max(); i++) {
            int dotX = x + (data.max() <= 8 ? i * 9 : i * (80 - 6) / (data.max() - 1));
            graphics.blitSprite(DOT, dotX, y + 3, 6, 6);
        }

        if (data.index() >= 0 && data.index() < data.max()) {
            int dotX = x + (data.max() <= 8 ? data.index() * 9 : data.index() * (80 - 6) / (data.max() - 1));
            graphics.blitSprite(FILLED_DOT, dotX, y + 3, 7, 7);
        }
    }
}