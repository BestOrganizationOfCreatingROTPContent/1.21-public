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
	
	public AbilityType(ResourceLocation registryKey, Function<AbilityId, A> constructor) {
		this.registryKey = registryKey;
		this.constructor = constructor;
	}
	
	public A createInstance(ResourceLocation powerTypeId, String nameInMoveset) {
		return createInstance(null, nameInMoveset);
	}
	
	public A createInstance(@Nullable Consumer<A> setParameters, AbilityId abilityId) {
		A ability = constructor.apply(abilityId);
		if (setParameters != null) setParameters.accept(ability);
		return ability;
	}
	
}
