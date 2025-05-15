package com.github.standobyte.jojo.mechanics.clothes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.utils.EnumUtil;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesSlotType;
import com.github.standobyte.jojo.util.entitycomponent.SynchronizableEntityData;
import com.github.standobyte.jojo.util.entitycomponent.TickingEntityData;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.network.PacketDistributor;

// TODO (clothes) drop clothes on entity death, or keep it on players if gamerule keepInventory is true
public class EntityClothesInventory implements SynchronizableEntityData, TickingEntityData, INBTSerializable<ListTag> {
	private final LivingEntity entity;
	private final Map<ClothesSlotType, ItemStack> items;
	private final Map<ClothesSlotType, ItemStack> lastItems;

	public EntityClothesInventory(LivingEntity entity) {
		this.entity = entity;
		this.items = EnumUtil.makeEnumMap(ClothesSlotType.class, () -> ItemStack.EMPTY);
		this.lastItems = EnumUtil.makeEnumMap(ClothesSlotType.class, () -> ItemStack.EMPTY);
		addTicking(entity);
	}

	public void setItemSlot(ClothesSlotType clothesSlot, ItemStack clothesCopy) {
		items.put(clothesSlot, clothesCopy);
	}

	public ItemStack getClothingPiece(ClothesSlotType clothesSlot) {
		return items.get(clothesSlot);
	}

	@Override
	public void tick() {
		serverTickUpdate(entity);
	}


	@ApiStatus.Internal
	protected void serverTickUpdate(LivingEntity entity) {
		if (entity.level().isClientSide()) return;

		Map<ClothesSlotType, ItemStack> changed = null;
		ServerLevel serverLevel = null;

		for (ClothesSlotType slot : ClothesSlotType.values()) {
			ItemStack oldItem = lastItems.get(slot);
			ItemStack newItem = getClothingPiece(slot);
			if (!ItemStack.matches(newItem, oldItem)) {
				if (changed == null) {
					changed = Maps.newEnumMap(ClothesSlotType.class);
				}
				changed.put(slot, newItem);
				if (serverLevel == null) serverLevel = (ServerLevel) entity.level();

				if (!oldItem.isEmpty()) {
					onOldItemRemoved(slot, oldItem, entity, serverLevel);
				}

				if (!newItem.isEmpty() && !newItem.isBroken()) {
					onNewItemWorn(slot, newItem, entity, serverLevel);
				}
			}
		}

		if (changed != null) {
			List<Pair<ClothesSlotType, ItemStack>> list = new ArrayList<>(changed.size());
			changed.forEach((slot, newItem) -> {
				ItemStack newItemCopy = newItem.copy();
				list.add(Pair.of(slot, newItemCopy));
				lastItems.put(slot, newItemCopy);
			});
			PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, new TrClothesItemsPacket(entity.getId(), list));
		}
	}

	// XXX (clothes) attributes
	protected void onOldItemRemoved(ClothesSlotType slot, ItemStack oldItem, LivingEntity entity, ServerLevel serverLevel) {
//		AttributeMap attributeMap = entity.getAttributes();
//		oldItem.forEachModifier(slot, (attribute, modifier) -> {
//			AttributeInstance attributeInstance = attributeMap.getInstance(attribute);
//			if (attributeInstance != null) {
//				attributeInstance.removeModifier(modifier);
//			}
//		});
//		EnchantmentHelper.stopLocationBasedEffects(oldItem, this, slot);
	}

	protected void onNewItemWorn(ClothesSlotType slot, ItemStack newItem, LivingEntity entity, ServerLevel serverLevel) {
//		AttributeMap attributeMap = entity.getAttributes();
//		newItem.forEachModifier(slot, (attribute, modifier) -> {
//			AttributeInstance attributeInstance = attributeMap.getInstance(attribute);
//			if (attributeInstance != null) {
//				attributeInstance.removeModifier(modifier.id());
//				attributeInstance.addTransientModifier(modifier);
//			}
//		});
//		EnchantmentHelper.runLocationChangedEffects(serverLevel, newItem, this, slot);
	}


	@Override
	public ListTag serializeNBT(HolderLookup.Provider provider) {
		ListTag itemsNbt = new ListTag();
		for (ClothesSlotType slot : ClothesSlotType.values()) {
			ItemStack item = items.get(slot);
			if (!item.isEmpty()) {
				itemsNbt.add(item.save(provider));
			} else {
				itemsNbt.add(new CompoundTag());
			}
		}
		return itemsNbt;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, ListTag itemsNbt) {
		for (ClothesSlotType slot : ClothesSlotType.values()) {
			CompoundTag itemNbt = itemsNbt.getCompound(slot.ordinal());
			items.put(slot, ItemStack.parseOptional(provider, itemNbt));
		}
		JojoMod.LOGGER.debug("loaded");
	}

	@Override
	public void syncToTracking(ServerPlayer trackingPlayer) {
		Map<ClothesSlotType, ItemStack> nonEmptyItems = null;

		for (ClothesSlotType slot : ClothesSlotType.values()) {
			ItemStack newItem = getClothingPiece(slot);
			if (!newItem.isEmpty()) {
				if (nonEmptyItems == null) {
					nonEmptyItems = Maps.newEnumMap(ClothesSlotType.class);
				}
				nonEmptyItems.put(slot, newItem);
			}
		}

		if (nonEmptyItems != null) {
			List<Pair<ClothesSlotType, ItemStack>> list = new ArrayList<>(nonEmptyItems.size());
			nonEmptyItems.forEach((slot, newItem) -> {
				list.add(Pair.of(slot, newItem));
			});
			PacketDistributor.sendToPlayer(trackingPlayer, new TrClothesItemsPacket(entity.getId(), list));
		}
	}


	@Nullable
	public static EntityClothesInventory getExisting(LivingEntity entity) {
		return entity != null && entity.hasData(ModDataAttachmentTypes.HUMANOID_CLOTHES.get()) ? 
				entity.getData(ModDataAttachmentTypes.HUMANOID_CLOTHES.get()) : null;
	}

}