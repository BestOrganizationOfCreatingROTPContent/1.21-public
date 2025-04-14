package com.github.standobyte.jojo.jojoimpl.zombie;

import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPower;

public class ZombieAbility extends Ability<PlayerPower> {

	public ZombieAbility(AbilityId abilityId) {
		super(abilityId);
	}
	
	
	@Override
	public final PowerClass<PlayerPower> getPowerClass() {
		return PowerClass.PLAYER_POWER;
	}

}
