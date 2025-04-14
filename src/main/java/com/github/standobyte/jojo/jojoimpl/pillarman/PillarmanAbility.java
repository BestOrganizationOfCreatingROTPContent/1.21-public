package com.github.standobyte.jojo.jojoimpl.pillarman;

import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPower;

public class PillarmanAbility extends Ability<PlayerPower> {

	public PillarmanAbility(AbilityId abilityId) {
		super(abilityId);
	}
	
	
	@Override
	public final PowerClass<PlayerPower> getPowerClass() {
		return PowerClass.PLAYER_POWER;
	}

}
