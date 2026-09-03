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
        return new Material(InventoryMenu.BLOCK_ATLAS, spriteId(texture != null ? texture : fallback));
    }

    /** Variants store textures as file paths, the block atlas indexes them as sprite ids. */
    private static ResourceLocation spriteId(ResourceLocation texture) {
        String path = texture.getPath();
        if (path.startsWith("textures/")) path = path.substring("textures/".length());
        if (path.endsWith(".png")) path = path.substring(0, path.length() - ".png".length());
        return ResourceLocation.fromNamespaceAndPath(texture.getNamespace(), path);
    }
}
