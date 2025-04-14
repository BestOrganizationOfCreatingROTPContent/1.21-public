package com.github.standobyte.jojo.client.entityanim;

import com.github.standobyte.jojo.client.entityrender.EntityActionRenderState;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.entityaction.ActionAnimIdentifier;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.util.entitycomponent.LivingAction;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.entity.LivingEntity;

//TODO (entity anims) player non-action animation (ActionAnimIdentifier and tick timestamp)
public class RotpPlayerRenderState {
	public EntityActionRenderState entityAction = new EntityActionRenderState();

	public static void extract(LivingEntity entity, HumanoidRenderState vanillaRenderState, RotpPlayerRenderState modRenderState, 
			float partialTick, ItemModelResolver itemModelResolver) {
		EntityActionInstance action = LivingAction.getUserAction(entity);
		EntityActionRenderState.extract(modRenderState.entityAction, action, partialTick);
//		modRenderState.entityAction.phaseTime = (entity.tickCount % 60) + partialTick;
		// TODO (entity anims) disable crouch
//		vanillaRenderState.isCrouching = false;
	}

	// TODO (entity anims) player action animation
	public static boolean setupAnim(HumanoidModel<?> model, HumanoidRenderState vanillaRenderState, RotpPlayerRenderState modRenderState) {
		EntityActionRenderState action = modRenderState.entityAction;
//		if (action.anim == null) return false;
//		JojoMod.LOGGER.debug("player_anim {} {} {} {}", action.anim.name, action.actionPhase, action.phaseTime, action.phaseCompletion);
		AnimWithExtras anim = AnimationLoader.getInstance().getAnim(JojoMod.resLoc("example_anim"), ActionAnimIdentifier.getOrCreate("dio_p3_wry"));
		if (anim == null) return false;
//		anim.animatePlayer(model, action.phaseTime, 1);
		return true;
	}
	
	public static interface IRotpRenderStateExtension {
		public RotpPlayerRenderState get(); 
	}
}
