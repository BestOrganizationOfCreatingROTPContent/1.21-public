package com.github.standobyte.jojo.powersystem.standpower;

import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;

public class StandAbility extends Ability<StandPower> {

	public StandAbility(AbilityId abilityId) {
		super(abilityId);
	}
	
	
	@Override
	public final PowerClass<StandPower> getPowerClass() {
		return PowerClass.STAND;
	}
}
