package com.github.standobyte.jojo.powersystem.entityaction;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.world.entity.LivingEntity;

public interface EntityActionAbility {
	@ApiStatus.OverrideOnly
	default EntityActionInstance createActionObj() {
		return new EntityActionInstance(this);
	}
	
	default LivingEntity getPerformer(LivingEntity user) {
		return user;
	}

	ActionAnimIdentifier getEntityAnim(EntityActionInstance action);
}
