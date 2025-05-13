package com.github.standobyte.jojo.jojoimpl.standentity;

import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionAbility;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;

public class StandEntityGrabThrowAbility extends StandEntityAbility {

	public StandEntityGrabThrowAbility(AbilityId abilityId) {
		super(abilityId);
	}
	
	
	@Override
	public EntityActionInstance createActionObj() {
		return new StandEntityGrabThrow(this);
	}
	
	public static class StandEntityGrabThrow extends EntityActionInstance {

		public StandEntityGrabThrow(EntityActionAbility ability) {
			super(ability);
		}
		
		@Override
		public void actionPerform() {
		}
		
	}

}
