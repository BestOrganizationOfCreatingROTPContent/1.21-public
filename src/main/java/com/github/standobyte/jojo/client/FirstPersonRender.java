package com.github.standobyte.jojo.client;

import com.github.standobyte.jojo.client.entityrender.stand.HumanoidPart;
import com.github.standobyte.jojo.client.entityrender.stand.StandEntityRenderState;
import com.github.standobyte.jojo.client.entityrender.stand.StandEntityRenderer;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;

public class FirstPersonRender {

	/**
	 * @return true if the vanilla hand render should be canceled entirely
	 */
	public static boolean onFirstPersonRender(Minecraft mc, float partialTick, PoseStack poseStack, BufferSource bufferSource, int light) {
		if (mc.cameraEntity != null && mc.cameraEntity != mc.player) {
			if (mc.cameraEntity instanceof StandEntity stand) {
				EntityRenderer<?> renderer = mc.getEntityRenderDispatcher().getRenderer(stand);
				
				StandEntityRenderer entityRenderer = (StandEntityRenderer) renderer;
				StandEntity _entity = (StandEntity) stand;
				StandEntityRenderState renderState = entityRenderer.createRenderState(_entity, partialTick);
				renderState.visibleParts = HumanoidPart.reduce(renderState.visibleParts, HumanoidPart.ARMS_ONLY);
				
				poseStack.pushPose();
				poseStack.mulPose(Axis.XP.rotationDegrees(renderState.xRot));
				poseStack.mulPose(Axis.YP.rotationDegrees(180 + renderState.bodyRot));
				poseStack.translate(0, -stand.getEyeHeight(), 0);
//				entityRenderer.render(renderState, poseStack, bufferSource, packedLight);
				entityRenderer.render(_entity, renderState, 0, partialTick, poseStack, bufferSource, light);
				poseStack.popPose();
				
				bufferSource.endBatch();
				return true;
			}
			
			return true;
		}
		
		return false;
	}
}
