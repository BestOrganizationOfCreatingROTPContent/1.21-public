package com.github.standobyte.jojo.powersystem.standpower.entity.abilities;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;

import net.minecraft.world.entity.LivingEntity;

public class StandEntityBarrageAbility extends StandEntityAbility<EntityActionInstance> {

	public StandEntityBarrageAbility(AbilityId abilityId) {
		super(abilityId);
		initPhaseLength(ActionPhase.WINDUP, 4);
		initPhaseLength(ActionPhase.PERFORM, 100);
		initPhaseLength(ActionPhase.RECOVERY, 10);
	}
	
	@Override
	public void tickEntityAction(EntityActionInstance action, LivingEntity performer, LivingEntity user) {
		if (action.getPhase() == ActionPhase.PERFORM) {
			JojoMod.LOGGER.debug("ORAORAORA");
		}
	}

}
