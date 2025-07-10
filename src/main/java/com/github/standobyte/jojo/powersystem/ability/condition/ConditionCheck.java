package com.github.standobyte.jojo.powersystem.ability.condition;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.powersystem.ability.Ability;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public class ConditionCheck {
	private final boolean positive;
//	private final boolean stopHeldAction;
//	private final boolean isQueued;
	private final Component warning;
	
	public static final ConditionCheck POSITIVE = new ConditionCheck(true, false, false, null);
	public static final ConditionCheck NEGATIVE = new ConditionCheck(false, true, false, null);
//	public static final ConditionCheck NEGATIVE_CONTINUE_HOLD = new ConditionCheck(false, false, false, null);
//	public static final ConditionCheck NEGATIVE_QUEUEABLE = new ConditionCheck(false, true, true, null);
	
	public static ConditionCheck createNegative(Component warning) {
		return new ConditionCheck(false, true, false, warning);
	}
	
	public static ConditionCheck noMessage(boolean isPositive) {
		return isPositive ? POSITIVE : NEGATIVE;
	}
	
	private ConditionCheck(boolean positive, boolean stopHeldAction, boolean isQueued, Component warning) {
		this.positive = positive;
//		this.stopHeldAction = stopHeldAction;
//		this.isQueued = isQueued;
		this.warning = warning;
	}
	
//	public ConditionCheck setContinueHold() {
//		return setContinueHold(true);
//	}
//	
//	public ConditionCheck setContinueHold(boolean continueHold) {
//		return new ConditionCheck(this.positive, !continueHold, this.isQueued, this.warning);
//	}
	
	public boolean isPositive() {
		return positive;
	}
	
//	public boolean shouldStopHeldAction() {
//		return !isPositive() && stopHeldAction;
//	}
//	
//	public boolean isQueued() {
//		return isQueued;
//	}
	
	@Nullable
	public Component getWarning() {
		return warning;
	}
	
	public static void sendActionFailedMessage(Ability ability, ConditionCheck result, LivingEntity user) {
		if (!user.level().isClientSide() /* && ability.sendsConditionMessage() */) {
			Component message = result.getWarning();
			
			if (message != null && user instanceof ServerPlayer player) {
				player.displayClientMessage(message, true);
			}
		}
	}

}
