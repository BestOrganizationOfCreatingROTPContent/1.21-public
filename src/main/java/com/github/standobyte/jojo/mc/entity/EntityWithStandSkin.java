package com.github.standobyte.jojo.mc.entity;

import java.util.Optional;

import net.minecraft.resources.ResourceLocation;

public interface EntityWithStandSkin {
	ResourceLocation getStandType();
	Optional<ResourceLocation> getStandSkin();
}
