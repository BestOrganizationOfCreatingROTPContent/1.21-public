package com.github.standobyte.jojo.client.entityrender.stand;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.entityanim.AnimWithExtras;
import com.github.standobyte.jojo.client.utils.ModelUtil;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.HumanoidArm;

public class StandEntityModel<T extends StandEntityRenderState> extends EntityModel<T> implements ArmedModel {
	public ModelPart left_arm_xrot;
	public ModelPart left_arm;
	public ModelPart right_arm_xrot;
	public ModelPart right_arm;
	public ModelPart head;
	public ModelPart torso_no_arms;
	public ModelPart torso_lower;
	public ModelPart left_leg_xrot;
	public ModelPart left_leg;
	public ModelPart right_leg_xrot;
	public ModelPart right_leg;
	protected Map<String, ModelPart[]> inheritanceChains = new HashMap<>();

	public StandEntityModel(ModelPart root) {
		super(root, RenderType::entityTranslucent);
		left_arm_xrot = getAnyDescendantWithName("left_arm_xrot").orElse(null);
		left_arm = getAnyDescendantWithName("left_arm").orElse(null);
		right_arm_xrot = getAnyDescendantWithName("right_arm_xrot").orElse(null);
		right_arm = getAnyDescendantWithName("right_arm").orElse(null);
		head = getAnyDescendantWithName("head").orElse(null);
		torso_no_arms = getAnyDescendantWithName("torso_no_arms").orElse(null);
		torso_lower = getAnyDescendantWithName("torso_lower").orElse(null);
		left_leg_xrot = getAnyDescendantWithName("left_leg_xrot").orElse(null);
		left_leg = getAnyDescendantWithName("left_leg").orElse(null);
		right_leg_xrot = getAnyDescendantWithName("right_leg_xrot").orElse(null);
		right_leg = getAnyDescendantWithName("right_leg").orElse(null);
		inheritanceChains = ModelUtil.modelPartInheritanceChains("root", this.root, "left_item", "right_item");
		// TODO (entity anim) make an array of all model parts that aren't visible by default
	}

	@Override
	public void setupAnim(T renderState) {
		super.setupAnim(renderState);

		this.setAllVisible(true);
		HumanoidPart.updateVisibility(this, renderState.visibleParts);
		
		AnimWithExtras anim = renderState.action.anim;
		float seconds = renderState.action.timeSeconds;
		if (anim != null) {
			anim.animate(this, renderState, seconds, 1);
		}
		
		// TODO (entity anim) iterate over the array of parts invisible by default - if a part was not animated, set visible to false
	}

	
	public void setAllVisible(boolean visible) {
		for (ModelPart modelPart : allParts()) {
			modelPart.visible = visible;
		}
	}
	
	public static void setVisible(@Nullable ModelPart modelPart, boolean visible) {
		if (modelPart != null) modelPart.visible = visible;
	}

	@Override
	public void translateToHand(HumanoidArm side, PoseStack poseStack) {
		var modelParts = switch (side) {
			case LEFT -> inheritanceChains.get("left_item");
			case RIGHT -> inheritanceChains.get("right_item");
		};
		if (modelParts != null) {
			for (ModelPart part : modelParts) {
				part.translateAndRotate(poseStack);
			}
			// counteract the vanilla transforms hardcoded in ItemInHandLayer
			poseStack.translate((float)(side == HumanoidArm.LEFT ? -1 : 1) / 16.0F, -0.5F, 0.125F);
		}
	}

}
