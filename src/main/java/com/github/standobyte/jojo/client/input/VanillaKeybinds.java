package com.github.standobyte.jojo.client.input;

import org.lwjgl.glfw.GLFW;

import com.github.standobyte.jojo.client.ui.jojomenu.IJojoMenuScreen;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.packet.fromclient.ClNoParamsPacket;
import com.github.standobyte.jojo.core.packet.fromclient.ClNoParamsPacket.PacketType;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.network.PacketDistributor;

public class VanillaKeybinds {
	public static final String MAIN_CATEGORY = "key.categories." + JojoMod.MOD_ID;
	public KeyMapping summonStand;
	public KeyMapping jojoStuffMenu;
	public static final String HUD_CATEGORY = "key.categories." + JojoMod.MOD_ID + ".hud";
//	public KeyMapping standHudMode;
//	public KeyMapping playerPowerHudMode;
	
	public static VanillaKeybinds register(RegisterKeyMappingsEvent event) {
		VanillaKeybinds binds = new VanillaKeybinds();
		event.register(binds.summonStand = new KeyMapping(
				JojoMod.MOD_ID + ".key.toggle_stand", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, MAIN_CATEGORY));
		event.register(binds.jojoStuffMenu = new KeyMapping(
				JojoMod.MOD_ID + ".key.jojo_menu", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_BACKSLASH, MAIN_CATEGORY));
//		event.register(binds.standHudMode = new KeyMapping(
//				JojoMod.MOD_ID + ".key.stand_mode", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, HUD_CATEGORY));
//		event.register(binds.playerPowerHudMode = new KeyMapping(
//				JojoMod.MOD_ID + ".key.non_stand_mode", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, HUD_CATEGORY));
		return binds;
	}
	
	public void handleTick() {
//		if (standHudMode.consumeClick()) {
//			actionsOverlay.switchMode(PowerClass.STAND);
//		}
//		
//		if (playerPowerHudMode.consumeClick()) {
//			actionsOverlay.switchMode(PowerClass.PLAYER_POWER);
//		}
//		
		if (summonStand.consumeClick()) {
//			if (standPower.hasPower() && !standPower.isActive()) {
//				actionsOverlay.onStandSummon();
//			}
			PacketDistributor.sendToServer(ClNoParamsPacket.of(PacketType.SUMMON_STAND));
		}
		
		if (jojoStuffMenu.consumeClick()) {
			IJojoMenuScreen.onScreenKeyPress();
		}
	}
	
}
