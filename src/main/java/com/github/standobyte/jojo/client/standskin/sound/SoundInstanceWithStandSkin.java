package com.github.standobyte.jojo.client.standskin.sound;

import java.util.Optional;

import net.minecraft.resources.ResourceLocation;

public interface SoundInstanceWithStandSkin {
	void jojoROASetStandSkin(ResourceLocation standId, Optional<ResourceLocation> standSkin);
}
