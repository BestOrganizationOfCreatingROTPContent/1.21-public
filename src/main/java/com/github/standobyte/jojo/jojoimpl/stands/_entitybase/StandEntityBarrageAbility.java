package com.github.standobyte.jojo.jojoimpl.stands._entitybase;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandOffsetFromUser;

import net.minecraft.world.phys.Vec3;

public class StandEntityBarrageAbility extends StandEntityAbility {

	public StandEntityBarrageAbility(AbilityId abilityId) {
		super(abilityId);
		setDefaultPhaseLength(ActionPhase.WINDUP, 4);
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
			if (performer instanceof StandEntity standEntity) {
				standEntity.offsetFromUser.setOffset(new Vec3(0, StandEntity.Y_OFFSET, 2), StandOffsetFromUser.OffsetMode.HEAD_XY, powerUser);
			}
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
				setPhase(ActionPhase.RECOVERY);
			}
		}
		
	}

}
