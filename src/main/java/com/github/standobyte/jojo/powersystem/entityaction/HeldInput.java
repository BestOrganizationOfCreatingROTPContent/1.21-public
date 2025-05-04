package com.github.standobyte.jojo.powersystem.entityaction;

import net.minecraft.world.entity.LivingEntity;

public interface HeldInput {
	void onStopHeld(LivingEntity user);
}
