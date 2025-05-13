package com.github.standobyte.jojo.client.entityrender;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.powersystem.entityaction.ActionAnimIdentifier;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;

import net.minecraft.world.entity.LivingEntity;

public class EntityActionRenderState {
	@Nullable public ActionAnimIdentifier anim;
	public float time = -1;
	
	@Nullable public ActionPhase actionPhase;
	public float phaseTime = -1;
	public float phaseCompletion = -1;
	
	public boolean disableCrouch = false;
	
	public static void extract(EntityActionRenderState renderState, LivingEntity performerEntity, @Nullable EntityActionInstance action, float partialTick) {
		if (action != null) {
			renderState.anim = action.ability.getEntityAnim(action);
			renderState.time = action.getFullTicksPassed() + partialTick;
			renderState.actionPhase = action.getPhase();
			renderState.phaseTime = action.getPhaseTick() + partialTick;
			renderState.phaseCompletion = action.getPhaseRatio(partialTick);
			renderState.disableCrouch = true;
		}
		else {
			renderState.anim = null;
			renderState.time = -1;
			renderState.actionPhase = null;
			renderState.phaseTime = -1;
			renderState.phaseCompletion = -1;
			renderState.disableCrouch = false;
		}
	}
}
