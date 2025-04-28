package com.github.standobyte.jojo.mixin.client.playeranim;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.client.entityanim.RipplesPlayerRenderState;
import com.github.standobyte.jojo.client.entityanim.RipplesPlayerRenderState.RipplesRenderStateExtensionMixin;

import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.entity.LivingEntity;

@Mixin(HumanoidMobRenderer.class)
public class HumanoidRendererMixin {

	@Inject(method = "extractHumanoidRenderState", at = @At("TAIL"))
	private static void jojo_ripples$addHumanoidRenderState(LivingEntity entity, HumanoidRenderState reusedState, 
			float partialTick, ItemModelResolver itemModelResolver, CallbackInfo ci) {
		RipplesPlayerRenderState.extract(entity, reusedState, ((RipplesRenderStateExtensionMixin) reusedState).get(), partialTick, itemModelResolver);
	}
	
}
