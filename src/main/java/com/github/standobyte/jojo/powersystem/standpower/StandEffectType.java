package com.github.standobyte.jojo.powersystem.standpower;

import net.minecraft.resources.ResourceLocation;

public class StandEffectType<T extends StandEffectInstance> {
	private final ResourceLocation registryKey;

	public StandEffectType(ResourceLocation registryKey) {
		this.registryKey = registryKey;
	}
	
	public ResourceLocation getRegistryKey() {
		return registryKey;
	}
	
}
