package com.github.standobyte.jojo.mixin.client.entitycontrol;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.client.entitycontrol.ClientEntityController;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

	@Inject(method = "renderHandsWithItems", at = @At("HEAD"), cancellable = true)
	public void jojo_ripples$renderControlledEntityHand(float partialTicks, PoseStack poseStack, BufferSource buffer, 
			LocalPlayer playerEntity, int combinedLight, CallbackInfo ci) {
		ClientEntityController controller = ClientEntityController.getInstance();
		if (controller != null && controller.renderFirstPerson(partialTicks, poseStack, buffer, combinedLight)) {
			ci.cancel();
		}
	}
}
