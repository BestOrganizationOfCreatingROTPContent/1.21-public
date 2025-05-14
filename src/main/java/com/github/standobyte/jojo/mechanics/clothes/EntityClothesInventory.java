package com.github.standobyte.jojo.mechanics.clothes;

import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.utils.EnumUtil;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesSlotType;
import com.github.standobyte.jojo.mechanics.clothes.mannequin.MannequinEntity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class EntityClothesInventory {
	private Map<ClothesSlotType, ItemStack> items = EnumUtil.makeEnumMap(ClothesSlotType.class, () -> ItemStack.EMPTY);

	public EntityClothesInventory(LivingEntity entity) {
		
	}

	public void setItemSlot(ClothesSlotType clothesSlot, ItemStack clothesCopy) {
		items.put(clothesSlot, clothesCopy);
	}

	public ItemStack getClothingPiece(ClothesSlotType clothesSlot) {
		return items.get(clothesSlot);
	}

	public CompoundTag serializeNBT() {
		CompoundTag nbt = new CompoundTag();
		
		return nbt;
	}

	public void deserializeNBT(CompoundTag nbt) {
		
	}
	
	
	@Nullable
	public static EntityClothesInventory get(LivingEntity entity) {
		if (entity instanceof MannequinEntity mannequin) {
			return mannequin.getClothes();
		}
		return null;
	}
	
}