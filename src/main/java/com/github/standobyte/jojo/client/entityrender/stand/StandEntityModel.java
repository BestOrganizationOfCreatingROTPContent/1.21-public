package com.github.standobyte.jojo.client.entityrender.stand;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.entityanim.AnimWithExtras;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;

public class StandEntityModel<T extends StandEntityRenderState> extends EntityModel<T> {
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
	}

	@Override
	public void setupAnim(T renderState) {
		super.setupAnim(renderState);
		
		AnimWithExtras anim = renderState.action.anim;
		float seconds = renderState.action.timeSeconds;
		if (anim != null) {
			anim.animate(this, renderState, seconds, 1);
		}
	}

	
	public void setAllVisible(boolean visible) {
		for (ModelPart modelPart : allParts()) {
			modelPart.visible = visible;
		}
	}
	
	public void updatePartsVisibility(VisibilityMode mode) {
		boolean setVisible = !mode.isInverted;

		if (mode.baseMode == VisibilityMode.ALL) {
			setAllVisible(setVisible);
		}
		else {
			setVisible(head, !setVisible);
			setVisible(torso_no_arms, !setVisible);
			setVisible(left_leg_xrot, !setVisible);
			setVisible(right_leg_xrot, !setVisible);
			setVisible(torso_lower, !setVisible);
			switch (mode.baseMode) {
			case ARMS_ONLY:
				setVisible(left_arm_xrot, setVisible);
				setVisible(right_arm_xrot, setVisible);
				break;
			case LEFT_ARM_ONLY:
				setVisible(left_arm_xrot, setVisible);
				setVisible(right_arm_xrot, !setVisible);
				break;
			case RIGHT_ARM_ONLY:
				setVisible(left_arm_xrot, !setVisible);
				setVisible(right_arm_xrot, setVisible);
				break;
			case NONE:
				setVisible(left_arm_xrot, !setVisible);
				setVisible(right_arm_xrot, !setVisible);
			default:
				break;
			}
		}
	}
	
	public static void setVisible(@Nullable ModelPart modelPart, boolean visible) {
		if (modelPart != null) modelPart.visible = visible;
	}

}
