package com.github.standobyte.jojo.client.entityrender;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.powersystem.entityaction.ActionAnimIdentifier;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;

public class EntityActionRenderState {
	@Nullable public ActionAnimIdentifier anim;
	@Nullable public ActionPhase actionPhase;
	public float phaseTime = -1;
	public float phaseCompletion = -1;
	
	public static void extract(EntityActionRenderState renderState, @Nullable EntityActionInstance action, float partialTick) {
		if (action != null) {
			renderState.anim = action.ability.getEntityAnim();
			renderState.actionPhase = action.getPhase();
			renderState.phaseTime = action.getTick() + partialTick;
			renderState.phaseCompletion = action.getPhaseRatio(partialTick);
		}
		else {
			renderState.anim = null;
			renderState.actionPhase = null;
			renderState.phaseTime = -1;
			renderState.phaseCompletion = -1;
		}
	}
}
