package com.github.standobyte.jojo.client.input;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.ability.Ability;

public class ClickHoldResolve {
	public static final float timeIsHold = 4; // 200 ms
	public static final float timeAssumeHold = 2; // 100 ms
	private InputState curState = null;
	private float timeHeld;
	
	public final Power<?> power;
	public final Ability clickAbility;
	public final Ability heldAbility;
	
	public ClickHoldResolve(Power<?> power, Ability heldAbility, Ability clickAbility) {
		this.power = power;
		this.clickAbility = clickAbility;
		this.heldAbility = heldAbility;
	}
	
	@Nullable
	public Result frameUpdate(float tickDelta) {
		timeHeld += tickDelta;
		InputState newState = timeHeld < timeAssumeHold ? null : timeHeld < timeIsHold ? InputState.ASSUME_HOLD : InputState.HOLD;
		if (newState != curState) {
			this.curState = newState;
			return new Result(newState, timeHeld);
		}
		return null;
	}
	
	public Result keyReleased() {
		return new Result(timeHeld < timeIsHold ? InputState.CLICK : InputState.HOLD, timeHeld);
	}
	
	
	public enum InputState {
		ASSUME_HOLD,
		HOLD,
		CLICK
	}
	
	public static record Result(InputState input, float timeTook) {}
}
