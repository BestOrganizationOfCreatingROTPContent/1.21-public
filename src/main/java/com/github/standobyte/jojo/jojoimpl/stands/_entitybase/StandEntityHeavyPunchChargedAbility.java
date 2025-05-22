package com.github.standobyte.jojo.jojoimpl.stands._entitybase;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandOffsetFromUser;

public class StandEntityHeavyPunchChargedAbility extends StandEntityAbility {

	public StandEntityHeavyPunchChargedAbility(AbilityId abilityId) {
		super(abilityId);
		setDefaultPhaseLength(ActionPhase.WINDUP, 16);
		setDefaultPhaseLength(ActionPhase.PERFORM, 8);
		setDefaultPhaseLength(ActionPhase.RECOVERY, 10);
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
		public void actionPerformStart() {
			setStandOffset(0, 2, StandOffsetFromUser.OffsetMode.HEAD_XY, false);
		}
		
		// TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! stop on the windup phase if the key hasn't been released

		@Override
		public void actionPerformEnd() {
			JojoMod.LOGGER.debug("ORAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
		}
	}
	

}
