package com.github.standobyte.jojo.jojoimpl.vampirism;

import static com.github.standobyte.jojo.core.JojoRegistries.ABILITY_TYPES;
import static com.github.standobyte.jojo.init.power.ModPlayerPowers.PLAYER_POWERS;

import com.github.standobyte.jojo.powersystem.Moveset;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPowerType;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;

public class VampirismPowerType extends PlayerPowerType<VampirismData> {

	public static final DeferredHolder<AbilityType<?>, AbilityType<VampirismAbility>> VAMPIRE_BLOOD_DRAIN = ABILITY_TYPES.register(
			"vampire_blood_drain", key -> new AbilityType<>(key, VampirismAbility::new));

	public static final DeferredHolder<PlayerPowerType<?>, VampirismPowerType> VAMPIRISM = PLAYER_POWERS.register(
			"vampirism", key -> new VampirismPowerType(key, new Moveset.Builder()
					.addAbility("blooddrain", VAMPIRE_BLOOD_DRAIN)));

	
	protected VampirismPowerType(ResourceLocation registryKey, Moveset.Builder abilitySet) {
		super(registryKey, abilitySet);
	}
	
	@Override
	public VampirismData newDataInstance() {
		return new VampirismData();
	}

}
