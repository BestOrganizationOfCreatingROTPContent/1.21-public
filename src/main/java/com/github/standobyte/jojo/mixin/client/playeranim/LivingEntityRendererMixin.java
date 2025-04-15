package com.github.standobyte.jojo.mixin.client.playeranim;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.client.entityanim.IHumanoidAnimModel;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    @Shadow EntityModel<?> model;

    @Inject(method = "render", at = @At(
	    		value = "INVOKE", 
	    		target = "Lnet/minecraft/client/model/EntityModel;setupAnim("
	    				+ "Lnet/minecraft/client/renderer/entity/state/EntityRenderState;"
	    				+ ")V",
				shift = Shift.AFTER))
	public void rotpAfterVanillaAnimSetup(LivingEntityRenderState renderState, 
			PoseStack poseStack, MultiBufferSource bufferSource, int light, CallbackInfo ci) {
		if (model instanceof IHumanoidAnimModel humanoidModel && renderState instanceof HumanoidRenderState humanoidRS) {
			humanoidModel.rotpSetupHumanoidAnim(humanoidRS);
		}
	}
}
