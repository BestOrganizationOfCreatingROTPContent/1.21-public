package com.github.standobyte.jojo.util.mc;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.v1_21_4_stuff.missingmethods._FriendlyByteBuf;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public record ContainerSlotInput(
		int containerId, 	// The id of the window which was clicked. 0 for player inventory.
		int slotNum 		// Id of the clicked slot
		) {
	
	@Nullable
	public static ContainerSlotInput cl_HoveredSlot() {
		if (Minecraft.getInstance().screen instanceof AbstractContainerScreen invScreen) {
			Slot slot = invScreen.getSlotUnderMouse();
			if (slot != null) {
				int index = switch (invScreen) {
					case CreativeModeInventoryScreen killMe -> {
						int anotherIndexWtfMojang = slot.getSlotIndex();
						if (anotherIndexWtfMojang >= 0 && anotherIndexWtfMojang <= 8) { // hotbar
							anotherIndexWtfMojang += 36;
						}
						else if (anotherIndexWtfMojang >= 45 && anotherIndexWtfMojang <= 53) { // hotbar when you have another creative tab open
							anotherIndexWtfMojang -= 9;
						}
						else if (anotherIndexWtfMojang >= 36 && anotherIndexWtfMojang <= 39) { // armor slots
							anotherIndexWtfMojang = 44 - anotherIndexWtfMojang;
						}
						yield anotherIndexWtfMojang;
					}
					
					default -> slot.index;
				};
				return new ContainerSlotInput(invScreen.getMenu().containerId, index);
			}
		}
		return null;
	}

	public static ItemStack getItem(ContainerSlotInput input, Player player) {
		if (player.containerMenu.containerId == input.containerId()) {
			if (!player.containerMenu.stillValid(player)) {
				JojoMod.getLogger().debug("Player {} interacted with invalid menu {}", player, player.containerMenu);
			} else {
				if (!player.containerMenu.isValidSlotIndex(input.slotNum())) {
					JojoMod.getLogger().debug("Player {} interacted with invalid slot index: {}, available slots: {}", 
							player.getName(), input.slotNum(), player.containerMenu.slots.size());
				} else {
					Slot slot = player.containerMenu.getSlot(input.slotNum());
					ItemStack item = slot.getItem();

//					if (input.stateId != player.containerMenu.getStateId()) {
//						player.containerMenu.broadcastFullState();
//					}

					return item;
				}
			}
		}

		return ItemStack.EMPTY;
	}

	public static final StreamCodec<? super FriendlyByteBuf, ContainerSlotInput> STREAM_CODEC = new StreamCodec<>() {
		
		@Override public void encode(FriendlyByteBuf buffer, ContainerSlotInput value) {
			_FriendlyByteBuf.writeContainerId(buffer, value.containerId());
			buffer.writeShort(value.slotNum());
		}
		
		@Override public ContainerSlotInput decode(FriendlyByteBuf buffer) {
			int containerId = _FriendlyByteBuf.readContainerId(buffer);
			int slotNum = buffer.readShort();
			return new ContainerSlotInput(containerId, slotNum);
		}
	};

}
