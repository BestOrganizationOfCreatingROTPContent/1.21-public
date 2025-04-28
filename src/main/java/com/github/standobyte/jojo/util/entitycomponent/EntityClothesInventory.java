package com.github.standobyte.jojo.util.entitycomponent;

import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesSlotType;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class EntityClothesInventory {

	public EntityClothesInventory(LivingEntity entity) {
		
	}

	public void setItemSlot(ClothesSlotType clothesSlot, ItemStack clothesCopy) {
		// TODO Auto-generated method stub
		
	}

	public ItemStack getClothingPiece(ClothesSlotType clothesSlot) {
		// TODO Auto-generated method stub
		return ItemStack.EMPTY;
	}

	public CompoundTag serializeNBT() {
		// TODO Auto-generated method stub
		CompoundTag nbt = new CompoundTag();
		
		return nbt;
	}

	public void deserializeNBT(CompoundTag nbt) {
		
	}
	
}