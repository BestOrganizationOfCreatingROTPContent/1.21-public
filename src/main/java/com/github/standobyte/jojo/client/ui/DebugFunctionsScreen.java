package com.github.standobyte.jojo.client.ui;

import com.github.standobyte.jojo.core.packet.fromclient.ClDebugCommandPacket;
import com.github.standobyte.jojo.mc.item.DebugItem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public class DebugFunctionsScreen extends Screen {

	public DebugFunctionsScreen() {
		super(CommonComponents.EMPTY);
	}
	
	public void init() {
		super.init();
		String[] commands = DebugItem.getOptions();
		for (int i = 0; i < commands.length; ++i) {
			String command = commands[i];
			Button button = new Button.Builder(Component.literal(command), b -> {
				boolean sendPacket = DebugItem.onClientClick(command);
				if (sendPacket) {
					PacketDistributor.sendToServer(new ClDebugCommandPacket(command));
				}
			})
			.pos(5, 5 + i * 25)
			.build();
			addRenderableWidget(button);
		}
	}
	
	public static void onDebugItemUsed() {
		Minecraft.getInstance().setScreen(new DebugFunctionsScreen());
	}

}
