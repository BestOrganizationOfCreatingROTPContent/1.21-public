package com.github.standobyte.jojo.jojoimpl.stands._entitybase;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandOffsetFromUser;

public class StandEntityBarrageAbility extends StandEntityAbility {

	public StandEntityBarrageAbility(AbilityId abilityId) {
		super(abilityId);
		setDefaultPhaseLength(ActionPhase.PERFORM, 100);
		setDefaultPhaseLength(ActionPhase.RECOVERY, 10);
	}
	
	
	@Override
	public EntityActionInstance createActionObj() {
		return new StandEntityBarrage(this);
	}
	
	public static class StandEntityBarrage extends EntityActionInstance {

		public StandEntityBarrage(EntityActionType ability) {
			super(ability);
		}
		
		@Override
		public void onActionSet() {
			setStandOffset(0, 1.5, StandOffsetFromUser.OffsetMode.HEAD_XY, true);
		}

		@Override
		public void actionTick() {
			if (getPhase() == ActionPhase.PERFORM) {
				JojoMod.LOGGER.debug("ORAORAORA");
			}
		}
		
		@Override
		public void onButtonStopHold() {
			if (getPhase() != ActionPhase.RECOVERY) {
				startPhase(ActionPhase.RECOVERY);
				syncPhaseChanges();
			}
		}
		
	}

}
