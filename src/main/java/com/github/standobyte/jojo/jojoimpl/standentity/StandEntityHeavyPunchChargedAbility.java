package com.github.standobyte.jojo.jojoimpl.standentity;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionAbility;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;

public class StandEntityHeavyPunchChargedAbility extends StandEntityAbility {

	public StandEntityHeavyPunchChargedAbility(AbilityId abilityId) {
		super(abilityId);
		setDefaultPhaseLength(ActionPhase.WINDUP, 10);
		setDefaultPhaseLength(ActionPhase.PERFORM, 10);
		setDefaultPhaseLength(ActionPhase.RECOVERY, 20);
	}
	
	
	@Override
	public EntityActionInstance createActionObj() {
		return new StandEntityChargedHeavy(this);
	}
	
	public static class StandEntityChargedHeavy extends EntityActionInstance {

		public StandEntityChargedHeavy(EntityActionAbility ability) {
			super(ability);
		}

		@Override
		public void actionPerform() {
			JojoMod.LOGGER.debug("ORAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
		}
	}
	

}
