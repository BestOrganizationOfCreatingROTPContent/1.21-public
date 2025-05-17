package com.github.standobyte.jojo.mechanics.clothes;

import java.util.List;

import com.github.standobyte.jojo.init.ModItemDataComponents;
import com.github.standobyte.jojo.mechanics.StoryPart;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesDataComponent;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

// TODO (clothes) putting on clothes via right click
// TODO (clothes) clothes inventory & UI
// TODO (clothes) callbacks when the clothes items are put on and taken off (similarly to the armor attributes)
public class ClothesItem extends Item {
	
	public ClothesItem(Item.Properties properties) {
		super(properties.stacksTo(1));
	}
	
	public ItemStack makeClothesPieceStack(ClothesDataComponent clothesData) {
		ItemStack stack = new ItemStack(this);
		stack.set(ModItemDataComponents.CLOTHES_PIECE.get(), clothesData);
		
		ResourceLocation itemModel = clothesData.getPiece().itemModel;
		if (itemModel != null) {
			stack.set(DataComponents.ITEM_MODEL, itemModel);
		}
		
		Component itemName = clothesData.getPiece().itemName;
		if (itemName != null) {
			stack.set(DataComponents.ITEM_NAME, itemName);
		}
		
		return stack;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
		ClothesDataComponent clothes = getPiece(stack);		if (clothes == null) return;
		var clothesSet = clothes.getClothesSet().value();	if (clothesSet == null) return;
		
		clothesSet.getStoryPart().ifPresent(storyPart -> {
			Component partName = StoryPart.partName(storyPart);
			if (partName != null) {
				tooltipComponents.add(partName);
			}
		});
	}

	public ClothesDataComponent getPiece(ItemStack itemStack) {
		return itemStack.get(ModItemDataComponents.CLOTHES_PIECE.get());
	}
	
}