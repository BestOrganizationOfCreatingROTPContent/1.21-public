package com.github.standobyte.jojo.client.entityrender.clothes;

import java.util.Map;

import com.github.standobyte.jojo.core.utils.EnumUtil;
import com.github.standobyte.jojo.mechanics.clothes.EntityClothesInventory;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesSlotType;
import com.github.standobyte.jojo.mechanics.clothes.mannequin.MannequinEntity;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class HumanoidClothesRSExtension {
	public final Map<ClothesSlotType, ItemStack> items = EnumUtil.makeEnumMap(ClothesSlotType.class, () -> ItemStack.EMPTY);
	public boolean slimModel;
	
	public final boolean extract(LivingEntity entity) {
		EntityClothesInventory entityClothes = EntityClothesInventory.getExisting(entity);
		if (entityClothes == null) return false;
		for (ClothesSlotType slot : ClothesSlotType.values()) {
			items.put(slot, entityClothes != null ? entityClothes.getClothingPiece(slot) : ItemStack.EMPTY);
		}
		slimModel = 
				(entity instanceof MannequinEntity mannequin && mannequin.isSlim()) || 
				(entity instanceof AbstractClientPlayer player && player.getSkin().model() == PlayerSkin.Model.SLIM);
		return true;
	}
	
	public static final HumanoidClothesRSExtension reusedInstance = new HumanoidClothesRSExtension();
}