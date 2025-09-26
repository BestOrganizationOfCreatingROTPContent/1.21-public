package com.github.standobyte.jojo.powersystem.standpower.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class StandEffectType<T extends StandEffectInstance> {
	public final ResourceLocation registryKey;
	protected IFactory<T> factory;

	public StandEffectType(ResourceLocation registryKey, IFactory<T> factory) {
		this.registryKey = registryKey;
		this.factory = factory;
	}

	@Deprecated
	public T create() {
		return create(null);
	}

	public T create(Level level) {
		T effect = factory.create(this);
		effect.level = level;
		return effect;
	}



	public interface IFactory<T extends StandEffectInstance> {
		T create(StandEffectType<T> effect);
	}
}
