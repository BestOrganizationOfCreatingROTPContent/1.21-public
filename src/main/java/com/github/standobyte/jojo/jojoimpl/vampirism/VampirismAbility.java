package com.github.standobyte.jojo.jojoimpl.vampirism;

import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPower;

public class VampirismAbility extends Ability<PlayerPower> {

	public VampirismAbility(AbilityId abilityId) {
		super(abilityId);
	}
	
	
	@Override
	public final PowerClass<PlayerPower> getPowerClass() {
		return PowerClass.PLAYER_POWER;
	}

}
