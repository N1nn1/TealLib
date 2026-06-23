package com.ninni.teallib.registry;

import com.ninni.teallib.TealLib;
import com.ninni.teallib.common.item.MannequinItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class TealItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, TealLib.MODID);

    public static final DeferredHolder<Item, Item> MANNEQUIN = ITEMS.register("mannequin", () -> new MannequinItem(new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));
}
