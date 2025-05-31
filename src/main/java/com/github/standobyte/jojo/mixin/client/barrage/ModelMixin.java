package com.github.standobyte.jojo.mixin.client.barrage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.Model;

@Mixin(Model.class)
public class ModelMixin {

	@Inject(method = "Lnet/minecraft/client/model/Model;renderToBuffer("
			+ "Lcom/mojang/blaze3d/vertex/PoseStack;"
			+ "Lcom/mojang/blaze3d/vertex/VertexConsumer;"
			+ "III)V", at = @At("TAIL"))
	public void jojo_ripples$renderBarrageSwings(PoseStack poseStack, VertexConsumer buffer, 
			int packedLight, int packedOverlay, int color, CallbackInfo ci) {}
}
