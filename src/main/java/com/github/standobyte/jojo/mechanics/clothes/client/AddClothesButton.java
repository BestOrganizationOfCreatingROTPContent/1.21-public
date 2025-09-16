package com.github.standobyte.jojo.mechanics.clothes.client;

import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.client.ui.widgets.ImageButton2;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.packet.fromclient.ClNoParamsPacket;
import com.github.standobyte.jojo.core.packet.fromclient.ClNoParamsPacket.PacketType;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.CommonComponents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class AddClothesButton {
	public static final GuiIcon BUTTON = new GuiIcon(JojoMod.resLoc("textures/gui/container/clothes/clothes_button.png"), 16, 16);
	public static final GuiIcon BUTTON_HOVERED = new GuiIcon(JojoMod.resLoc("textures/gui/container/clothes/clothes_button_hovered.png"), 16, 16);
	
	@SubscribeEvent(priority = EventPriority.NORMAL)
	public static void addButton(ScreenEvent.Init.Post event) {
		Screen screen = event.getScreen();
		if (screen instanceof EffectRenderingInventoryScreen containerScreen) {
			Button clothesButton = new ImageButton2(
					containerScreen.getGuiLeft() + 153, containerScreen.getGuiTop() + 63, 16, 16, 
					BUTTON, BUTTON, BUTTON_HOVERED, BUTTON_HOVERED, 
					b -> {
						PacketDistributor.sendToServer(ClNoParamsPacket.of(PacketType.OPEN_CLOTHES));
					},
					CommonComponents.EMPTY);
			event.addListener(clothesButton);
		}
	}

//  while (this.options.keyInventory.consumeClick()) {
//      if (this.gameMode.isServerControlledInventory()) {
//          this.player.sendOpenInventory();
//      } else {
//          this.tutorial.onOpenInventory();
//          this.setScreen(new InventoryScreen(this.player));
//      }
//  }
}
