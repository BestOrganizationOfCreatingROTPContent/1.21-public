package com.github.standobyte.jojo.client.input;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.input.controlscheme.ClientKeyWrapper;

import net.neoforged.neoforge.client.settings.KeyModifier;

public class HeldKeyTimer {
	public final ClientKeyWrapper key;
	public final boolean cancelVanilla;
	public final KeyModifier modifier;
	@Nullable public ClickHoldResolve clickHoldResolve;
	private int ticks;
	
	public HeldKeyTimer(ClientKeyWrapper key, boolean cancelVanilla, KeyModifier modifier) {
		this.key = key;
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
