package com.github.standobyte.jojo.util.mc;

import com.github.standobyte.jojo.util.mc.ActionTarget.TargetType;

import net.minecraft.world.level.Level;

/**
 * Aim target that is constantly synchronized from player client to server
 */
public class ActionTargetAim {
	protected ActionTarget target = ActionTarget.EMPTY;
	protected ActionTarget targetPrev = ActionTarget.EMPTY;
	
	/**
	 * @return true if the target has changed
	 */
	public boolean setTarget(ActionTarget target) {
		if (!target.equals(this.target)) {
			this.targetPrev = this.target;
			this.target = target;
			return true;
		}
		return false;
	}
	
	public boolean setTargetOnSync(ActionTarget target, Level level) {
		if (target.getType() == TargetType.ENTITY) {
			target = target.resolveEntityId(level);
		}
		return setTarget(target);
	}
	
	public ActionTarget getTarget() {
		return target;
	}
	
	
	// TODO (entity aim) different aim target update methods
	/*
	 * mob/NPC stand - TICK_ON_SERVER (for head rotation)
	 * mob/NPC - ON_DEMAND
	 */
	public enum TargetUpdateMethod {
		TICK_PLAYER_CLIENT,
		TICK_SERVER,
		ON_DEMAND
	}
	
}
