package com.github.standobyte.jojo.powersystem.standpower.entity.abilities;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;

import net.minecraft.world.entity.LivingEntity;

public class StandEntityHeavyPunchChargedAbility extends StandEntityAbility<EntityActionInstance> {

	public StandEntityHeavyPunchChargedAbility(AbilityId abilityId) {
		super(abilityId);
		initPhaseLength(ActionPhase.WINDUP, 10);
		initPhaseLength(ActionPhase.PERFORM, 10);
		initPhaseLength(ActionPhase.RECOVERY, 20);
	}
	
	@Override
	public void entityPerform(EntityActionInstance action, LivingEntity performer, LivingEntity user) {
		JojoMod.LOGGER.debug("ORAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
	}

}
