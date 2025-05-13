package com.github.standobyte.jojo.client.entityanim;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.entityrender.EntityActionRenderState;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.util.entitycomponent.LivingAction;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class RipplesPlayerRenderState {
	@Nullable public ResourceLocation animSet;
	public EntityActionRenderState entityAction = new EntityActionRenderState();

	public static void extract(LivingEntity entity, HumanoidRenderState vanillaRenderState, RipplesPlayerRenderState modRenderState, 
			float partialTick, ItemModelResolver itemModelResolver) {
		EntityActionInstance action = LivingAction.getUserAction(entity);
		EntityActionRenderState.extract(modRenderState.entityAction, entity, action, partialTick);
		
		modRenderState.animSet = null;
		if (action != null) {
			modRenderState.animSet = action.ability.getEntityAnimSet(entity);
		}
		
		if (modRenderState.entityAction.disableCrouch) vanillaRenderState.isCrouching = false;
	}

	public static boolean setupAnim(HumanoidModel<?> model, HumanoidRenderState vanillaRenderState, RipplesPlayerRenderState modRenderState) {
		EntityActionRenderState action = modRenderState.entityAction; 								if (action.anim == null || modRenderState.animSet == null) return false;
		AnimationSet animSet = AnimationLoader.getInstance().getAnimSet(modRenderState.animSet); 	if (animSet == null) return false;
		AnimWithExtras anim = animSet.getNamedAnim(action.anim); 									if (anim == null) return false;
		anim.animateVanillaPlayer(model, vanillaRenderState, action, 1);
		return true;
	}
	
	public static interface RipplesRenderStateExtensionMixin {
		public RipplesPlayerRenderState get(); 
	}
}
