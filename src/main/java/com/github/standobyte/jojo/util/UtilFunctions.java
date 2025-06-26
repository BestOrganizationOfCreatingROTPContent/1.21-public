package com.github.standobyte.jojo.util;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class UtilFunctions {

	public static Iterable<Entity> getEntities(Level level) {
		if (level instanceof ClientLevel clientLevel) {
			return clientLevel.entitiesForRendering();
		}
		if (level instanceof ServerLevel serverLevel) {
			return serverLevel.getAllEntities();
		}
		throw new IllegalArgumentException();
	}
	
}
