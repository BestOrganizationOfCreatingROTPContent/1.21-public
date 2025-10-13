package com.github.standobyte.jojo.mechanics.entitycontrol.client.mob;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

public class MobControlUtil {

	public static Mob getMobOrMobVehicle(Entity controlledEntity) {
		if (controlledEntity != null) {
			Entity vehicle = controlledEntity.getControlledVehicle();
			if (vehicle instanceof Mob mob) {
				return mob;
			}
			else if (controlledEntity instanceof Mob mob) {
				return mob;
			}
		}
		
		return null;
	}
}
