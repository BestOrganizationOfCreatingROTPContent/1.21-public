package com.github.standobyte.jojo.client;

import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.PlayerModelPart;

public class ClientUtil {
	public static final int MAX_LIGHT = 0xF000F0;

	public static void setCameraEntityPreventShaderSwitch(Entity entity) {
		Minecraft mc = Minecraft.getInstance();
		mc.setCameraEntity(entity);
		// XXX prevent shader switch
//		if (mc.gameRenderer.currentEffect() == null) {
//			ShaderEffectApplier.getInstance().updateCurrentShader();
//		}
	}

	public static float partialTick(DeltaTracker fuckThis, boolean worksInPauseToo) {
		return fuckThis.getGameTimeDeltaPartialTick(worksInPauseToo);
	}

	public static void renderPlayerFace(PoseStack poseStack, int x, int y, AbstractClientPlayer player) {
		Minecraft mc = Minecraft.getInstance();
		PlayerSkin playerSkin = player.getSkin();
		ResourceLocation playerFace = playerSkin.texture();
		BlitFloat.blit(poseStack, mc, playerFace, 
				x, y, 16, 16, 0, 
				16, 16, 16, 16, 128, 128, 
				BlitFloat.NO_TINT);
		if (player.isModelPartShown(PlayerModelPart.HAT)) {
			poseStack.pushPose();
			poseStack.translate(x, y, 0);
			poseStack.scale(9F/8F, 9F/8F, 0);
			poseStack.translate(-1, -1, 0);
			BlitFloat.blit(poseStack, mc, playerFace, 
					x, y, 16, 16, 0, 
					80, 16, 16, 16, 128, 128, 
					BlitFloat.NO_TINT);
			poseStack.popPose();
		}
	}

}
