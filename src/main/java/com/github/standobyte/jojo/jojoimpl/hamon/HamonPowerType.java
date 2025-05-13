package com.github.standobyte.jojo.jojoimpl.hamon;

import static com.github.standobyte.jojo.core.JojoRegistries.ABILITY_TYPES;
import static com.github.standobyte.jojo.init.power.ModPlayerPowers.PLAYER_POWERS;

import com.github.standobyte.jojo.jojoimpl.hamon.abilities.HamonOverdriveBeatAbility;
import com.github.standobyte.jojo.jojoimpl.hamon.abilities.HamonRebuffOverdriveAbility;
import com.github.standobyte.jojo.powersystem.MovesetBuilder;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPowerType;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;

public class HamonPowerType extends PlayerPowerType<HamonData> {

	public static final DeferredHolder<AbilityType<?>, AbilityType<Ability>> HAMON_BEAT = ABILITY_TYPES.register(
			"hamon_beat", key -> new AbilityType<>(key, HamonOverdriveBeatAbility::new));

	public static final DeferredHolder<AbilityType<?>, AbilityType<Ability>> REBUFF_OVERDRIVE = ABILITY_TYPES.register(
			"rebuff_overdrive", key -> new AbilityType<>(key, HamonRebuffOverdriveAbility::new));

	public static final DeferredHolder<PlayerPowerType<?>, HamonPowerType> HAMON = PLAYER_POWERS.register(
			"hamon", key -> new HamonPowerType(key, new MovesetBuilder()
					.addAbility("hamon_beat", HAMON_BEAT)
					.addAbility("rebuff_overdrive", REBUFF_OVERDRIVE)));

	
	protected HamonPowerType(ResourceLocation registryKey, MovesetBuilder abilitySet) {
		super(registryKey, abilitySet);
	}
	
	@Override
	public HamonData newDataInstance() {
		return new HamonData();
	}

}
