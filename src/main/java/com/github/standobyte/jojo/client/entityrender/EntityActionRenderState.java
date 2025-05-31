package com.github.standobyte.jojo.client.entityrender;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.entityanim.AnimWithExtras;
import com.github.standobyte.jojo.client.entityanim.AnimWithExtras.TimelineKeys;
import com.github.standobyte.jojo.client.entityanim.RipplesPlayerRenderState;
import com.github.standobyte.jojo.client.entityanim.RipplesPlayerRenderState.RipplesRenderStateExtensionMixin;
import com.github.standobyte.jojo.client.entityanim.barrage.BarrageSwings;
import com.github.standobyte.jojo.client.entityrender.stand.StandEntityRenderState;
import com.github.standobyte.jojo.powersystem.entityaction.ActionAnimIdentifier;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;

public class EntityActionRenderState {
	@Nullable public ActionAnimIdentifier animId;
	public float time = -1;
	@Nullable public ActionPhase actionPhase;
	public float phaseTime = -1;
	public float phaseCompletion = -1;
	public boolean disableCrouch = false;

	public AnimWithExtras anim;
	public float timeSeconds;
	@Nullable public BarrageSwings barrageSwings;


	public static void extract(EntityActionRenderState renderState, LivingEntity performerEntity, @Nullable EntityActionInstance action, float partialTick) {
		if (action != null) {
			renderState.animId = action.ability.getEntityAnim(action);
			renderState.time = action.getFullTicksPassed() + partialTick;
			renderState.actionPhase = action.getPhase();
			renderState.phaseTime = action.getPhaseTick() + partialTick;
			renderState.phaseCompletion = action.getPhaseRatio(partialTick);
			renderState.disableCrouch = true;
		}
		else {
			renderState.animId = null;
			renderState.time = -1;
			renderState.actionPhase = null;
			renderState.phaseTime = -1;
			renderState.phaseCompletion = -1;
			renderState.disableCrouch = false;
		}
		
		renderState.anim = null;
		renderState.timeSeconds = 0;
		renderState.barrageSwings = null;
	}
	
	public static void setAnim(EntityActionRenderState renderState, LivingEntityRenderState vanillaRenderState, 
			AnimWithExtras anim, @Nullable BarrageSwings barrageSwings) {
		renderState.anim = anim;
		renderState.timeSeconds = 0;
		renderState.barrageSwings = barrageSwings;
		if (anim != null) {
			renderState.timeSeconds = anim.getAnimTime(renderState);
			if (barrageSwings != null) {
				String barrageType = anim.instructionTimelines.getStringTimelineVal(TimelineKeys.BARRAGE, renderState.timeSeconds);
				barrageSwings.frameStandBarrage(Minecraft.getInstance(), anim, barrageType, renderState.timeSeconds, vanillaRenderState);
			}
		}
	}
	
	
	
	
	@Nullable
	public static EntityActionRenderState getFrom(LivingEntityRenderState vanillaRenderState) {
		if (vanillaRenderState instanceof StandEntityRenderState standEntity) {
			return standEntity.action;
		}
		if (vanillaRenderState instanceof RipplesRenderStateExtensionMixin playerMixin) {
			RipplesPlayerRenderState playerExtension = playerMixin.get();
			return playerExtension != null ? playerExtension.entityAction : null;
		}
		return null;
	}
}
