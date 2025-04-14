package com.github.standobyte.jojo.jojoimpl.zombie;

import static com.github.standobyte.jojo.core.JojoRegistries.ABILITY_TYPES;
import static com.github.standobyte.jojo.init.power.ModPlayerPowers.PLAYER_POWERS;

import com.github.standobyte.jojo.powersystem.Moveset;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPower;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPowerType;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ZombiePowerType extends PlayerPowerType<ZombieData> {

	public static final DeferredHolder<AbilityType<?>, AbilityType<ZombieAbility>> ZOMBIE_DEVOUR = ABILITY_TYPES.register(
			"zombie_devour", key -> new AbilityType<>(key, ZombieAbility::new));

	public static final DeferredHolder<PlayerPowerType<?>, ZombiePowerType> ZOMBIE = PLAYER_POWERS.register(
			"zombie", key -> new ZombiePowerType(key, new Moveset.Builder<PlayerPower>()
					.addAbility("devour", ZOMBIE_DEVOUR)));

	
	protected ZombiePowerType(ResourceLocation registryKey, Moveset.Builder<PlayerPower> abilitySet) {
		super(registryKey, abilitySet);
	}
	
	@Override
	public ZombieData newDataInstance() {
		return new ZombieData();
	}

}
