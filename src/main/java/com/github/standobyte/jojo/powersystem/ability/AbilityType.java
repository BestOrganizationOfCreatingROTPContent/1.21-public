package com.github.standobyte.jojo.powersystem.ability;

import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.resources.ResourceLocation;

@ApiStatus.NonExtendable
public class AbilityType<A extends Ability> {
	public final ResourceLocation registryKey;
	protected Function<AbilityId, A> constructor;
	public final Ability _defaultAbilityInstance;
	
	public AbilityType(ResourceLocation registryKey, Function<AbilityId, A> constructor) {
		this.registryKey = registryKey;
		this.constructor = constructor;
		this._defaultAbilityInstance = AbilityId.makeDefaultAbilityInstance(this);//constructor.apply(new AbilityId(null, null, registryKey.toString()));
	}
	
	public A createInstance(@Nullable Consumer<A> setParameters, AbilityId abilityId) {
		A ability = constructor.apply(abilityId);
		if (setParameters != null) setParameters.accept(ability);
		return ability;
	}
	
}
