package com.github.standobyte.jojo.mechanics.entityuseitem;

import com.github.standobyte.jojo.init.ModSpecialActions;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.entityaction.type.SpecialEntityActionType;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

/**
 * An action for when a Stand clicks specific items, for example, when it throws a projectile,
 * to be able to do things like set the Stand's offset to the front of the user and then reset it.
 */
public class VanillaItemClickAsAction extends SpecialEntityActionType {

	public VanillaItemClickAsAction(ResourceLocation id) {
		super("", id);
	}
	
	// Anim set returned by this method overwrites the vanilla player poses, we don't want that
	@Override
	public ResourceLocation getEntityAnimSet(LivingEntity user) {
		return null;
	}

	@Override
	public EntityActionInstance createActionObj() {
		return new ItemClickInstance(this);
	}

	public static class ItemClickInstance extends EntityActionInstance {
		protected boolean isPlayerEntity;
		protected ServerPlayer standUserPlayer;

		public ItemClickInstance() {
			this(ModSpecialActions.RMB_CLICK_ITEM.get());
			this.setStartingPhase();
		}

		protected ItemClickInstance(EntityActionType ability) {
			super(ability);
			phasesLength.put(ActionPhase.RECOVERY, 10f);
		}
		
		@Override
		public boolean canBeCancelledInto(EntityActionType cancellingAbility) {
			return true;
		}
		
	}
	
}
