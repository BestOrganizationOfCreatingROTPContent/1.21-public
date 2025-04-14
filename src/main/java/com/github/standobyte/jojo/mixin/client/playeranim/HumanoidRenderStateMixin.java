package com.github.standobyte.jojo.mixin.client.playeranim;

import org.spongepowered.asm.mixin.Mixin;

import com.github.standobyte.jojo.client.entityanim.RotpPlayerRenderState;
import com.github.standobyte.jojo.client.entityanim.RotpPlayerRenderState.IRotpRenderStateExtension;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

@Mixin(HumanoidRenderState.class)
public class HumanoidRenderStateMixin implements IRotpRenderStateExtension {
	private final RotpPlayerRenderState rotpRenderState = new RotpPlayerRenderState();

	@Override
	public RotpPlayerRenderState get() {
		return rotpRenderState;
	}
	
}
