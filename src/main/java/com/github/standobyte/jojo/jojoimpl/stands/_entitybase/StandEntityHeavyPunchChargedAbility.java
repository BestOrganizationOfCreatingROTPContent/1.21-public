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
		setDefaultPhaseLength(ActionPhase.BUTTON_CHARGE, 16);
		setDefaultPhaseLength(ActionPhase.WINDUP, 999999);
		setDefaultPhaseLength(ActionPhase.PERFORM, 6);
		setDefaultPhaseLength(ActionPhase.RECOVERY, 12);
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
		public void onButtonStopHold() {
			switch (getPhase()) {
				case BUTTON_CHARGE -> {
					phasesLength.put(ActionPhase.WINDUP, 0f);
					syncPhaseChanges();
				}
				case WINDUP -> {
					startPhase(ActionPhase.PERFORM);
					syncPhaseChanges();
				}
				default -> {}
			}
		}
		
		@Override
		public void actionPerformStart() {
			setStandOffset(0, 2, StandOffsetFromUser.OffsetMode.HEAD_XY, false);
		}
		
		@Override
		public void actionPerformEnd() {
			JojoMod.LOGGER.debug("ORAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
		}
		
		@Override
		public boolean canBeCancelledInto(EntityActionType cancellingAbility) {
			return phase.ordinal() < ActionPhase.PERFORM.ordinal();
		}
		
	}

}
