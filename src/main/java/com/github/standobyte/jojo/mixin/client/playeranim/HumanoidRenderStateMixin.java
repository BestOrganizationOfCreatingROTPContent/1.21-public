package com.github.standobyte.jojo.mixin.client.playeranim;

import org.spongepowered.asm.mixin.Mixin;

import com.github.standobyte.jojo.client.entityanim.RipplesPlayerRenderState;
import com.github.standobyte.jojo.client.entityanim.RipplesPlayerRenderState.RipplesRenderStateExtensionMixin;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

@Mixin(HumanoidRenderState.class)
public class HumanoidRenderStateMixin implements RipplesRenderStateExtensionMixin {
	private final RipplesPlayerRenderState jojo_ripples$renderState = new RipplesPlayerRenderState();

	@Override
	public RipplesPlayerRenderState get() {
		return jojo_ripples$renderState;
	}
	
}
