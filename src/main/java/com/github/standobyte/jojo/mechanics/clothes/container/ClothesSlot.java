package com.github.standobyte.jojo.mechanics.clothes.container;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesDataComponent;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesSlotType;
import com.mojang.datafixers.util.Pair;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ClothesSlot extends Slot {
	private final LivingEntity owner;
	private final ClothesSlotType slot;
	@Nullable
	private final ResourceLocation emptyIcon;

	public ClothesSlot(
			Container container, LivingEntity owner, ClothesSlotType slot, int slotIndex, int x, int y, @Nullable ResourceLocation emptyIcon
			) {
		super(container, slotIndex, x, y);
		this.owner = owner;
		this.slot = slot;
		this.emptyIcon = emptyIcon;
	}

	@Override
	public void setByPlayer(ItemStack newItem, ItemStack oldItem) {
		ClothesDataComponent.onEquipItem(owner, this.slot, oldItem, newItem);
		super.setByPlayer(newItem, oldItem);
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}

	/**
	 * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
	 */
	@Override
	public boolean mayPlace(ItemStack stack) {
		return ClothesSlotType.canEquip(stack, slot);
	}

//	/**
//	 * Return whether this slot's stack can be taken from this slot.
//	 */
//	@Override
//	public boolean mayPickup(Player player) {
//		ItemStack itemstack = this.getItem();
//		return !itemstack.isEmpty() && !player.isCreative() && EnchantmentHelper.has(itemstack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)
//				? false : super.mayPickup(player);
//	}

	@Override
	public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
		return this.emptyIcon != null ? Pair.of(InventoryMenu.BLOCK_ATLAS, this.emptyIcon) : super.getNoItemIcon();
	}
}
