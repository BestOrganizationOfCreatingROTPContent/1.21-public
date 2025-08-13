package com.github.standobyte.jojo.client.item;

import java.util.HashMap;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.ModItemDataComponents;
import com.github.standobyte.jojo.init.ModItems;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ItemIconModels {
	public static final ResourceLocation MOD_LOGO = JojoMod.resLoc("mod_logo");
	protected static HashMap<ResourceLocation, ItemStack> cache = new HashMap<>();
	
	public static ItemStack makeIconItem(ResourceLocation model) {
		return cache.computeIfAbsent(model, modelPath -> {
			ItemStack item = new ItemStack(ModItems.DEBUG_ITEM.get());
			item.set(ModItemDataComponents.ITEM_MODEL.get(), modelPath);
			return item;
		});
	}
}
