package com.github.standobyte.jojo.client.entityrender.entities;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.world.item.component.ResolvableProfile;

public class MannequinRenderState extends ArmorStandRenderState {
	public boolean isSlim;
	public boolean hasSkull;
	@Nullable public ResolvableProfile playerProfile;
}
