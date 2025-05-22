package com.github.standobyte.jojo.jojoimpl.stands._entitybase;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandOffsetFromUser;

public class StandEntityHeavyPunchAbility extends StandEntityAbility {

	public StandEntityHeavyPunchAbility(AbilityId abilityId) {
		super(abilityId);
		setDefaultPhaseLength(ActionPhase.WINDUP, 8);
		setDefaultPhaseLength(ActionPhase.PERFORM, 4);
		setDefaultPhaseLength(ActionPhase.RECOVERY, 20);
	}
	
	
	@Override
	public EntityActionInstance createActionObj() {
		return new StandEntityHeavyPunch(this);
	}
	
	public static class StandEntityHeavyPunch extends EntityActionInstance {

		public StandEntityHeavyPunch(EntityActionType ability) {
			super(ability);
		}
		
		@Override
		public void onActionSet() {
			setStandOffset(0, 2, StandOffsetFromUser.OffsetMode.HEAD_XY, false);
		}

		@Override
		public void actionPerformEnd() {
			JojoMod.LOGGER.debug("ORAAAAA");
		}
	}

}
