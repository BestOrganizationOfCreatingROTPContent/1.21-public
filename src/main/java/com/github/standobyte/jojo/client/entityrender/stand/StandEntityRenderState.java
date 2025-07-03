package com.github.standobyte.jojo.client.entityrender.stand;

import com.github.standobyte.jojo.client.entityrender.EntityActionRenderState;
import com.github.standobyte.jojo.client.standskin.StandSkin;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.ResourceLocation;

public class StandEntityRenderState extends HumanoidRenderState {
	public ResourceLocation standId;
	public StandSkin defaultSkin;
	public StandSkin skin;
	public final EntityActionRenderState action = new EntityActionRenderState();
	public int tint = -1;
}
