package com.github.standobyte.jojo.jojoimpl.standentity;

import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;

public class StandEntityGrabAbility extends StandEntityAbility {

	public StandEntityGrabAbility(AbilityId abilityId) {
		super(abilityId);
	}
	
	
	@Override
	public EntityActionInstance createActionObj() {
		return new StandEntityGrab(this);
	}
	
	public static class StandEntityGrab extends EntityActionInstance {

		public StandEntityGrab(EntityActionType ability) {
			super(ability);
		}
		
		@Override
		public void actionPerform() {
		}
		
	}

}
