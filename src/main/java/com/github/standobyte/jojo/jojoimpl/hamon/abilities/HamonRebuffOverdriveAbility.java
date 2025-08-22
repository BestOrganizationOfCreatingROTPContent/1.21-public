package com.github.standobyte.jojo.jojoimpl.hamon.abilities;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.EntityActionAbility;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;

public class HamonRebuffOverdriveAbility extends EntityActionAbility {

	public HamonRebuffOverdriveAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId);
		setDefaultPhaseLength(ActionPhase.WINDUP, 12);
		setDefaultPhaseLength(ActionPhase.PERFORM, 8);
		setDefaultPhaseLength(ActionPhase.RECOVERY, 5);
	}
	
	@Override
	public EntityActionInstance createActionObj() {
		return new HamonRebuffOverdrive(this);
	}

	public static class HamonRebuffOverdrive extends EntityActionInstance {
		
		public HamonRebuffOverdrive(EntityActionType ability) {
			super(ability);
		}

		@Override
		public void actionPerformStart() {
			JojoMod.LOGGER.debug("REBUFF OVERDRIVE");
		}
	}
	
}
