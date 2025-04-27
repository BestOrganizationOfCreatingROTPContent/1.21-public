package com.github.standobyte.jojo.jojoimpl.pillarman;

import static com.github.standobyte.jojo.core.JojoRegistries.ABILITY_TYPES;
import static com.github.standobyte.jojo.init.power.ModPlayerPowers.PLAYER_POWERS;

import com.github.standobyte.jojo.powersystem.Moveset;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPowerType;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;

public class PillarmanPowerType extends PlayerPowerType<PillarmanData> {

	public static final DeferredHolder<AbilityType<?>, AbilityType<PillarmanAbility>> PILLAR_MAN_ABSORPTION = ABILITY_TYPES.register(
			"pillar_man_absorption", key -> new AbilityType<>(key, PillarmanAbility::new));

	public static final DeferredHolder<PlayerPowerType<?>, PillarmanPowerType> PILLAR_MAN = PLAYER_POWERS.register(
			"pillar_man", key -> new PillarmanPowerType(key, new Moveset.Builder()
					.addAbility("absorb", PILLAR_MAN_ABSORPTION)));

	
	protected PillarmanPowerType(ResourceLocation registryKey, Moveset.Builder abilitySet) {
		super(registryKey, abilitySet);
	}
	
	@Override
	public PillarmanData newDataInstance() {
		return new PillarmanData();
	}

}
