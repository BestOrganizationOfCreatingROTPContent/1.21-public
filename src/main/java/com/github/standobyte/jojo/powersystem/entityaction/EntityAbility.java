package com.github.standobyte.jojo.powersystem.entityaction;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.util.entitycomponent.LivingAction;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public interface EntityAbility<A extends EntityActionInstance, P extends Power<P>> {
	
	/**
	 * Call this in onClick() or in onButtonStartHold() to make the user do an EntityActionInstance.
	 */
	default A setEntityAction(Level level, LivingEntity user) {
		A action = createEntityAction();
		// onClick() and onButtonStartHold() calls are already sent to other clients, 
		// that's why we don't need to sync the EntityActionInstance inside LivingAction
		LivingAction.getComponent(user).setAction(action, false);
		return action;
	}
	
	@SuppressWarnings("unchecked")
	default A createEntityAction() {
		return (A) new EntityActionInstance(this);
	}
	
	default void onActionSet(A action, LivingEntity performer, P power) {}
	
	default void tickEntityAction(A action, LivingEntity performer, P power) {}
	
	default void entityPerform(A action, LivingEntity performer, P power) {}
	
	default void onActionCleared(A action, LivingEntity performer, P power) {}
	
	@ApiStatus.Internal
	default void onEntityActionTick(A action, LivingEntity performer, P power) {
		tickEntityAction(action, performer, power);
		if (action.phase == ActionPhase.PERFORM && action.getTicksLeft() == 1) {
			entityPerform(action, performer, power);
		}
	}
	
	
	// TODO (!) (entity action) partial tick for consecutive actions
	default float getPhaseLength(ActionPhase phase) {
		return phase == ActionPhase.PERFORM ? 1 : 0;
	}
	
	
	ActionAnimIdentifier getEntityAnim();
	
	PowerClass<P> getPowerClass();
	
}
