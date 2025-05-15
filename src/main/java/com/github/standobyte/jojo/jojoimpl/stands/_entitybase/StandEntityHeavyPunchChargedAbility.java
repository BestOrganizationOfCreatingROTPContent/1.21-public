package com.github.standobyte.jojo.jojoimpl.stands._entitybase;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
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

		public StandEntityChargedHeavy(EntityActionType ability) {
			super(ability);
		}

		@Override
		public void actionPerform() {
			JojoMod.LOGGER.debug("ORAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
		}
	}
	

}
