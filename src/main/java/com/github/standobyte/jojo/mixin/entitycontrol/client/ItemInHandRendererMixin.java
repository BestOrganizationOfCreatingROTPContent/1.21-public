package com.github.standobyte.jojo.mixin.entitycontrol.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.client.FirstPersonRender;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
	@Shadow @Final private Minecraft minecraft;

	@Inject(method = "<init>", at = @At("TAIL"))
	public void jojo_ripples$on1stPersonRendererInit(CallbackInfo ci) {
		FirstPersonRender.init();
	}

	@Inject(method = "tick", at = @At("TAIL"))
	public void jojo_ripples$on1stPersonRendererTick(CallbackInfo ci) {
		FirstPersonRender.getInstance().tick();
	}

	@Inject(method = "renderHandsWithItems", at = @At("HEAD"), cancellable = true)
	public void jojo_ripples$renderControlledEntityHand(float partialTicks, PoseStack poseStack, BufferSource buffer, 
			LocalPlayer playerEntity, int combinedLight, CallbackInfo ci) {
		if (FirstPersonRender.onFirstPersonRender(minecraft, partialTicks, poseStack, buffer, combinedLight)) {
			ci.cancel();
		}
	}
}
