package com.github.standobyte.jojo.mechanics.entity_like_player.useitem;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.ModSpecialActions;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.entityaction.type.SpecialEntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Lets the mod's action system know that the entity is in the middle of using an item via the vanilla system
 * (drawing a bow, charging a crossbow, eating, etc.).
 */
public class VanillaItemUseAsAction extends SpecialEntityActionType {

	public VanillaItemUseAsAction(ResourceLocation id) {
		super("", id);
		// Anim set overwrites the vanilla player poses, we don't want that
		this.animSet = null;
	}

	@Override
	public EntityActionInstance createActionObj() {
		return new ItemUsingInstance(this);
	}

	public static class ItemUsingInstance extends EntityActionInstance {
		protected boolean isPlayerEntity;
		protected ServerPlayer standUserPlayer;

		public ItemUsingInstance() {
			this(ModSpecialActions.RMB_USING_ITEM.get());
			this.setStartingPhase();
		}

		protected ItemUsingInstance(EntityActionType ability) {
			super(ability);
			phasesLength.put(ActionPhase.PERFORM, 72000f);
			phasesLength.put(ActionPhase.RECOVERY, 10f);
		}
		
		@Override
		public void onActionSet(@Nullable EntityActionInstance prevAction) {
			isPlayerEntity = performer instanceof Player;
			standUserPlayer = performer instanceof StandEntity stand && stand.getUser() instanceof ServerPlayer player ? player : null;
		}
		
		@Override
		public void actionTick() { // force stop the action if the entity has stopped using the item
			if (!level().isClientSide() && phase == ActionPhase.PERFORM && !performer.isUsingItem()) {
				setPhase(ActionPhase.RECOVERY, 0);
				syncPhaseChanges();
			}
		}
		
		@Override
		public void onButtonStopHold() { // lets stands shoot bows and throw tridents when the client releases RMB
			if (!level().isClientSide() && !isPlayerEntity) {
				ServerSideLivingClick.releaseUsingItem(performer, standUserPlayer);
				setPhase(ActionPhase.RECOVERY, 0);
				syncPhaseChanges();
			}
		}
		
		@Override
		public boolean canBeCancelledInto(EntityActionType cancellingAbility) {
			return true;
		}
		
	}
	
}
