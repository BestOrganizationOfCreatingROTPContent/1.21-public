package com.github.standobyte.jojo.mechanics.entitycontrol.client.mob;

import java.util.function.Predicate;

import javax.annotation.Nullable;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

public class HardcodedMobControlCommands {

	protected static enum WitchPotionMode { DRINK, SPLASH }
	public static ItemStack[] WITCH_DRINK_POTIONS = new ItemStack[] {
			PotionContents.createItemStack(Items.POTION, Potions.HEALING),
			PotionContents.createItemStack(Items.POTION, Potions.FIRE_RESISTANCE),
			PotionContents.createItemStack(Items.POTION, Potions.SWIFTNESS),
			PotionContents.createItemStack(Items.POTION, Potions.WATER_BREATHING),
	};
	public static ItemStack[] WITCH_SPLASH_POTIONS = new ItemStack[] {
			PotionContents.createItemStack(Items.SPLASH_POTION, Potions.HARMING),
			PotionContents.createItemStack(Items.SPLASH_POTION, Potions.HEALING),
			PotionContents.createItemStack(Items.SPLASH_POTION, Potions.REGENERATION),
			PotionContents.createItemStack(Items.SPLASH_POTION, Potions.SLOWNESS),
			PotionContents.createItemStack(Items.SPLASH_POTION, Potions.POISON),
			PotionContents.createItemStack(Items.SPLASH_POTION, Potions.WEAKNESS),
	};
	@Nullable
	public static ItemStack[] getWitchPotions(@Nullable WitchPotionMode mode) {
		if (mode == null) return null;
		return switch (mode) {
			case SPLASH -> WITCH_SPLASH_POTIONS;
			case DRINK -> WITCH_DRINK_POTIONS;
		};
	}
	
	
	public static void onHotbarPacket(LivingEntity entity, ClControlledMobCommandPacket.CommandType commandType, int slot) {
		switch (commandType) {
			case PICK_SLOT ->  {
				
			}
			/* VERY careful with this one, this MUST NOT remove any player-made items that the mob might have picked up
			 * (either on its own or when controlled by a player) to not potentially enable griefing
			 */
			case EMPTY_MAIN_HAND -> {
				if (entity instanceof Witch) {
					clearHeldItem(entity, heldItem -> {
						Item item = heldItem.getItem();
						return item == Items.POTION || item == Items.SPLASH_POTION;
					});
				}
			}
			case SWAP_ITEMS ->  {
				
			}
			case TOSS ->  {
				
			}
			case WITCH_PICK_DRINK_POTION ->  {
				if (entity instanceof Witch) {
					if (slot >= 0 && slot < WITCH_DRINK_POTIONS.length) {
						entity.setItemInHand(InteractionHand.MAIN_HAND, WITCH_DRINK_POTIONS[slot].copy());
					}
					else {
						clearHeldItem(entity, heldItem -> {
							Item item = heldItem.getItem();
							return item == Items.POTION || item == Items.SPLASH_POTION;
						});
					}
				}
			}
			case WITCH_PICK_SPLASH_POTION ->  {
				if (entity instanceof Witch) {
					if (slot >= 0 && slot < WITCH_SPLASH_POTIONS.length) {
						entity.setItemInHand(InteractionHand.MAIN_HAND, WITCH_SPLASH_POTIONS[slot].copy());
					}
					else {
						clearHeldItem(entity, heldItem -> {
							Item item = heldItem.getItem();
							return item == Items.POTION || item == Items.SPLASH_POTION;
						});
					}
				}
			}
			default -> {}
		}
	}
	
	protected static void clearHeldItem(LivingEntity entity, Predicate<ItemStack> condition) {
		ItemStack heldItem = entity.getMainHandItem();
		if (!heldItem.isEmpty() && condition.test(heldItem)) {
			entity.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		}
	}
	
}
