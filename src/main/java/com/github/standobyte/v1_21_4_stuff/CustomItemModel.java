package com.github.standobyte.v1_21_4_stuff;

import com.github.standobyte.jojo.init.ModItemDataComponents;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class CustomItemModel {

	public static void set(ItemStack item, ResourceLocation itemModel) {
		item.set(ModItemDataComponents.ITEM_MODEL.get()/*DataComponents.ITEM_MODEL*/, itemModel);
	}
}
