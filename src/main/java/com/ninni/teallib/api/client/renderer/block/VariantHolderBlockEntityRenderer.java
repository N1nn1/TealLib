package com.ninni.teallib.api.client.renderer.block;

import com.ninni.teallib.api.common.data.variant.VariantManager;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.Nameable;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;


public abstract class VariantHolderBlockEntityRenderer<T extends BlockEntity & Nameable> implements BlockEntityRenderer<T> {

    @SuppressWarnings("SameParameterValue")
    protected Material getVariantMaterial(T be, ResourceLocation fallback) {
        ResourceLocation texture = VariantManager.getTexture(be, "default");
        if (texture != null) return new Material(InventoryMenu.BLOCK_ATLAS, texture);
        return new Material(InventoryMenu.BLOCK_ATLAS, fallback);
    }
}
