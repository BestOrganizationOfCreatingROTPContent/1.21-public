package com.github.standobyte.jojo.powersystem.standpower.entity.abilities;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;

import net.minecraft.world.entity.LivingEntity;

public class StandEntityPunchAbility extends StandEntityAbility<EntityActionInstance> {

	public StandEntityPunchAbility(AbilityId abilityId) {
		super(abilityId);
		initPhaseLength(ActionPhase.WINDUP, 4);
	}
	
	@Override
	public void entityPerform(EntityActionInstance action, LivingEntity performer, StandPower power) {
		JojoMod.LOGGER.debug("ORA");
	}

}
