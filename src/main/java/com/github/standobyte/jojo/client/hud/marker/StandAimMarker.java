package com.github.standobyte.jojo.client.hud.marker;

import java.util.List;

import com.github.standobyte.jojo.client.hud.AdditionalHud;
import com.github.standobyte.jojo.client.input.AimTarget;
import com.github.standobyte.jojo.client.utils.ui.GuiIcon;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.util.mc.ActionTarget;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class StandAimMarker extends MarkerRenderer {
	public static final GuiIcon AIM_ICON = new GuiIcon(AdditionalHud.UI_ELEMENTS, 224, 0, 17, 17, 256, 256);

	@Override
	protected boolean shouldRender() {
		// TODO (stand aim marker) after fixing the depth issue, enable it in the build
		if (JojoMod.disableDevStuff() || mc.player == null) return false;
		StandPower standPower = StandPower.get(mc.player);
		return standPower != null && standPower.getSummonedStandEntity() != null;
	}

	@Override
	protected void updatePositions(List<MarkerInstance> list, float partialTick) {
		ActionTarget target = AimTarget.standAimTarget;
		switch (target.getType()) {
			case BLOCK -> list.add(new MarkerInstance(Vec3.atCenterOf(target.getBlockPos())));
			case ENTITY -> {
				Entity entity = target.getEntity();
				list.add(new MarkerInstance(entity.getPosition(partialTick).add(0, entity.getBbHeight() / 2, 0)));
			}
			default -> {}
		}
	}

	@Override
	protected void renderAt(PoseStack poseStack, MarkerInstance marker, Camera camera, Vec3 diff, float partialTick, int[] argb) {
		poseStack.pushPose();

		double distance = diff.length();
		float scale = Math.min((float) Math.pow(2, (16 - Math.min(distance, 32)) / 16) * (float) distance / 256, 1);

		poseStack.translate(diff.x, diff.y, diff.z);
		poseStack.scale(-scale * 1.25f, -scale * 1.25f, 1);

		AIM_ICON.render(poseStack, -AIM_ICON.width / 2, -AIM_ICON.height / 2, 255, 255, 255, 255);

		poseStack.popPose();
	}

}
