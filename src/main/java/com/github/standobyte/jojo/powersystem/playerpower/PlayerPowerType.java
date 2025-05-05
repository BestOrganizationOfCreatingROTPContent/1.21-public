package com.github.standobyte.jojo.powersystem.playerpower;

import javax.annotation.Nonnull;

import com.github.standobyte.jojo.powersystem.MovesetBuilder;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.PowerType;

import net.minecraft.resources.ResourceLocation;

public abstract class PlayerPowerType<D extends PowerData> extends PowerType {
	private final ResourceLocation registryKey;

	public PlayerPowerType(ResourceLocation registryKey, MovesetBuilder abilitySet) {
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
