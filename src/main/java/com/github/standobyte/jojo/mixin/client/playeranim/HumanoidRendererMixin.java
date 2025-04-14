package com.github.standobyte.jojo.mixin.client.playeranim;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.client.entityanim.RotpPlayerRenderState;
import com.github.standobyte.jojo.client.entityanim.RotpPlayerRenderState.IRotpRenderStateExtension;

import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.entity.LivingEntity;

@Mixin(HumanoidMobRenderer.class)
public class HumanoidRendererMixin {

	@Inject(method = "extractHumanoidRenderState", at = @At("TAIL"))
	private static void rotpAddHumanoidRenderState(LivingEntity entity, HumanoidRenderState reusedState, 
			float partialTick, ItemModelResolver itemModelResolver, CallbackInfo ci) {
		RotpPlayerRenderState.extract(entity, reusedState, ((IRotpRenderStateExtension) reusedState).get(), partialTick, itemModelResolver);
	}
	
}
