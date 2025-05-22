package com.github.standobyte.jojo.jojoimpl.hamon.abilities;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.EntityActionAbility;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;

public class HamonOverdriveBeatAbility extends EntityActionAbility {

	public HamonOverdriveBeatAbility(AbilityId abilityId) {
		super(abilityId);
		setDefaultPhaseLength(ActionPhase.WINDUP, 5);
		setDefaultPhaseLength(ActionPhase.PERFORM, 3);
		setDefaultPhaseLength(ActionPhase.RECOVERY, 2);
	}
	
	@Override
	public EntityActionInstance createActionObj() {
		return new HamonOverdriveBeat(this);
	}

	public static class HamonOverdriveBeat extends EntityActionInstance {
		
		public HamonOverdriveBeat(EntityActionType ability) {
			super(ability);
		}

		@Override
		public void actionPerformStart() {
			JojoMod.LOGGER.debug("НЫА");
		}
	}
	
}
