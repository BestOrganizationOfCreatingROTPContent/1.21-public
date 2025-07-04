package com.github.standobyte.jojo.util;

import com.github.standobyte.jojo.client.ClientProxy;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class UtilFunctions {

	public static Iterable<Entity> getEntities(Level level) {
		if (level instanceof ServerLevel serverLevel) {
			return serverLevel.getAllEntities();
		}
		if (level.isClientSide()) {
			return ClientProxy.getEntities(level);
		}
		throw new IllegalArgumentException();
	}
	
}
