package com.github.standobyte.jojo.mixin.client.playeranim;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.Model;

@Mixin(Model.class)
public abstract class ModelMixin {

	@Inject(method = "renderToBuffer("
			+ "Lcom/mojang/blaze3d/vertex/PoseStack;"
			+ "Lcom/mojang/blaze3d/vertex/VertexConsumer;"
			+ "III)V", at = @At("HEAD"), cancellable = true)
	public void jojo_ripples$renderWithBends(PoseStack poseStack, VertexConsumer buffer, 
			int packedLight, int packedOverlay, int color, CallbackInfo ci) {}

	@Inject(method = "resetPose()V", at = @At("HEAD"))
	public void jojo_ripples$resetPose(CallbackInfo ci) {}
	
}
