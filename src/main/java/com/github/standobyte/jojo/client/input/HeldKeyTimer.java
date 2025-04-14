package com.github.standobyte.jojo.client.input;

import javax.annotation.Nullable;

import net.neoforged.neoforge.client.settings.KeyModifier;

public class HeldKeyTimer {
	public final short keyId;
	public final boolean cancelVanilla;
	public final KeyModifier modifier;
	@Nullable public ClickHoldResolve clickHoldResolve;
	private int ticks;
	
	public HeldKeyTimer(short keyId, boolean cancelVanilla, KeyModifier modifier) {
		this.keyId = keyId;
		this.cancelVanilla = cancelVanilla;
		this.modifier = modifier;
		this.ticks = 0;
	}
	
	public void incTicks() {
		++ticks;
	}
	
	public int getTicks() {
		return ticks;
	}
	
}
