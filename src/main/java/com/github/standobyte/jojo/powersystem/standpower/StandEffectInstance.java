package com.github.standobyte.jojo.powersystem.standpower;

import javax.annotation.Nonnull;

public class StandEffectInstance {
	@Nonnull public final StandEffectType<?> effectType;
	
	
	public StandEffectInstance(@Nonnull StandEffectType<?> effectType) {
		this.effectType = effectType;
	}

}
