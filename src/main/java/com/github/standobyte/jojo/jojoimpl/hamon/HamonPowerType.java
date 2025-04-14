package com.github.standobyte.jojo.jojoimpl.hamon;

import static com.github.standobyte.jojo.core.JojoRegistries.ABILITY_TYPES;
import static com.github.standobyte.jojo.init.power.ModPlayerPowers.PLAYER_POWERS;

import com.github.standobyte.jojo.powersystem.Moveset;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPower;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPowerType;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;

public class HamonPowerType extends PlayerPowerType<HamonData> {

	public static final DeferredHolder<AbilityType<?>, AbilityType<HamonAbility>> HAMON_OVERDRIVE = ABILITY_TYPES.register(
			"hamon_overdrive", key -> new AbilityType<>(key, HamonAbility::new));

	public static final DeferredHolder<PlayerPowerType<?>, HamonPowerType> HAMON = PLAYER_POWERS.register(
			"hamon", key -> new HamonPowerType(key, new Moveset.Builder<PlayerPower>()
					.addAbility("overdrive", HAMON_OVERDRIVE)));

	
	protected HamonPowerType(ResourceLocation registryKey, Moveset.Builder<PlayerPower> abilitySet) {
		super(registryKey, abilitySet);
	}
	
	@Override
	public HamonData newDataInstance() {
		return new HamonData();
	}

}
