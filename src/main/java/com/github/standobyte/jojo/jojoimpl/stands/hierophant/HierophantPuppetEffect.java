package com.github.standobyte.jojo.jojoimpl.stands.hierophant;

import com.github.standobyte.jojo.powersystem.standpower.effect.StandEffectInstance;
import com.github.standobyte.jojo.powersystem.standpower.effect.StandEffectType;

public class HierophantPuppetEffect extends StandEffectInstance {

	public HierophantPuppetEffect(StandEffectType<?> effectType) {
		super(effectType);
		needsTarget = true;
	}

	@Override
	protected void start() {}

	@Override
	protected void tick() {}

	@Override
	protected void stop() {}

}
