package com.github.standobyte.jojo.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class ClientProxy {

	public static Player getClientPlayer() {
		Minecraft mc = Minecraft.getInstance();
		return mc.player;
	}

	public static Level getClientWorld() {
		Minecraft mc = Minecraft.getInstance();
		return mc.level;
	}
	
	public static Vec3 getCameraPos() {
		Minecraft mc = Minecraft.getInstance();
		return mc.gameRenderer.getMainCamera().getPosition();
	}

	public static Entity getEntityById(int entityId) {
		Minecraft mc = Minecraft.getInstance();
		return mc.level.getEntity(entityId);
	}
	
	public static void openScreen(Screen screen) {
		Minecraft.getInstance().setScreen(screen);
	}
	
}
