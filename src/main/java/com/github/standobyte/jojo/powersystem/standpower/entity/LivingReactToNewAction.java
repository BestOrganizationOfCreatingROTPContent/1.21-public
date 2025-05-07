package com.github.standobyte.jojo.powersystem.standpower.entity;

import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;

public interface LivingReactToNewAction {
	/**
	 * @param action - The new entity action, before it is set.
	 * @return true to cancel the action - it will not be set to the entity.
	 */
	boolean onActionSet(EntityActionInstance action);
}
