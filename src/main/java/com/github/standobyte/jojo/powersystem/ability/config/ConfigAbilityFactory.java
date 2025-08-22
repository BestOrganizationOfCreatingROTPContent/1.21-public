package com.github.standobyte.jojo.powersystem.ability.config;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;

@ApiStatus.Internal
public class ConfigAbilityFactory<A extends Ability> {
	private final AbilityType<A> abilityType;
	@Nullable private Consumer<A> abilityConfig;

	public ConfigAbilityFactory(AbilityType<A> abilityType, @Nullable Consumer<A> abilityConfig) {
		this.abilityType = abilityType;
		this.abilityConfig = abilityConfig;
	}
	
	public ConfigAbilityFactory<A> copy() {
		return new ConfigAbilityFactory<>(abilityType, abilityConfig);
	}
	
	public void addConfig(Consumer<A> extraConfig) {
		if (this.abilityConfig == null) {
			this.abilityConfig = extraConfig;
		}
		else {
			this.abilityConfig = this.abilityConfig.andThen(extraConfig);
		}
	}

	public A makeAbility(AbilityId abilityId) {
		A ability = abilityType.createInstance(abilityId);
		if (abilityConfig != null) {
			abilityConfig.accept(ability);
		}
		return ability;
	}
	
}
