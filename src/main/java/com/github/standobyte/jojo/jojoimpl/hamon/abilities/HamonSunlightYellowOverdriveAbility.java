package com.github.standobyte.jojo.jojoimpl.hamon.abilities;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.EntityActionAbility;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionType;

public class HamonSunlightYellowOverdriveAbility extends EntityActionAbility {

	public HamonSunlightYellowOverdriveAbility(AbilityId abilityId) {
		super(abilityId);
		setDefaultPhaseLength(ActionPhase.WINDUP, 40);
		setDefaultPhaseLength(ActionPhase.PERFORM, 10);
		setDefaultPhaseLength(ActionPhase.RECOVERY, 7);
	}
	
	@Override
	public EntityActionInstance createActionObj() {
		return new SYOverdrive(this);
	}

	public static class SYOverdrive extends EntityActionInstance {
		
		public SYOverdrive(EntityActionType ability) {
			super(ability);
		}
		
		@Override
		public void onButtonStopHold() {
			if (getPhase() == ActionPhase.WINDUP) {
				if (getPhaseTick() >= 10) {
					setPhase(ActionPhase.PERFORM);
				}
				else {
					forceStop();
				}
			}
		}

		@Override
		public void actionPerform() {
			JojoMod.LOGGER.debug("НЫЫЫЫЫЫЫЫЫЫЫЫЫЫЫЫЫЫЫЫАААААААААА");
		}
	}
	
}
