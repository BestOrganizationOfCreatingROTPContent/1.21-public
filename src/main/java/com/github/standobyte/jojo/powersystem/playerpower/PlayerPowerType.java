package com.github.standobyte.jojo.powersystem.playerpower;

import javax.annotation.Nonnull;

import com.github.standobyte.jojo.powersystem.Moveset;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.PowerType;

import net.minecraft.resources.ResourceLocation;

public abstract class PlayerPowerType<D extends PowerData> extends PowerType<PlayerPower> {
	private final ResourceLocation registryKey;

	public PlayerPowerType(ResourceLocation registryKey, Moveset.Builder<PlayerPower> abilitySet) {
		super(abilitySet);
		this.registryKey = registryKey;
	}
	
	@Nonnull
	public abstract D newDataInstance();

	
	@Override
	public ResourceLocation getId() {
		return registryKey;
	}
	
	@Override
	public PowerClass<PlayerPower> getPowerClass() {
		return PowerClass.PLAYER_POWER;
	}
	
}
