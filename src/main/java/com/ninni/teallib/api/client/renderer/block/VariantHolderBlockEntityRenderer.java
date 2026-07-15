package com.ninni.teallib.api.client.renderer.block;

import com.ninni.teallib.api.common.block.entity.VariantHolderBlockEntity;
import com.ninni.teallib.api.common.data.blockvariant.BlockVariantManager;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.Optional;


public abstract class VariantHolderBlockEntityRenderer<T extends VariantHolderBlockEntity> implements BlockEntityRenderer<T> {

    @SuppressWarnings("SameParameterValue")
    protected Material getVariantTexture(T be, String subfolder, String extra) {
        ResourceLocation variant = be.getVariant();
        String beName = Objects.requireNonNull(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(be.getType())).getPath();
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(variant.getNamespace(), "block_entity/" + beName + "/"  + subfolder + beName + "_" + variant.getPath() + extra);
        Level level = be.getLevel();
        if (level != null) {
            Optional<BlockVariantManager.BlockVariantData> nameTagOverride = BlockVariantManager.getNameTagOverride(level.registryAccess(), be);
            if (nameTagOverride.isPresent()) texture = ResourceLocation.fromNamespaceAndPath(nameTagOverride.get().id().getNamespace(), "block_entity/" + beName + "/" + subfolder + beName + "_" + nameTagOverride.get().id().getPath() + extra);
        }
        return new Material(InventoryMenu.BLOCK_ATLAS, texture);
    }
}
