package com.github.standobyte.jojo.client.input;

import java.util.function.Function;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.input.controlscheme.ClientKeyWrapper;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.mojang.datafixers.util.Either;

import net.minecraft.Util;
import net.neoforged.neoforge.client.settings.KeyModifier;

public class HeldKeyTimer {
	public final ClientKeyWrapper key;
	public final boolean cancelVanilla;
	public final KeyModifier modifier;
	private int ticks;
	public Either<InputMethod, ClickHoldResolve> inputMethod = unambiguous.apply(InputMethod.HOLD);
	
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


	protected static final Function<InputMethod, Either<InputMethod, ClickHoldResolve>> unambiguous = Util.memoize(Either::left);
	public void setResolveInputMethod(ClickHoldResolve inputMethod) {
		this.inputMethod = Either.right(inputMethod);
	}
	
	@Nullable
	public ClickHoldResolve getResolvingInputMethod() {
		return this.inputMethod.right().orElse(null);
	}
	
	public void setInputMethod(InputMethod inputMethod) {
		this.inputMethod = unambiguous.apply(InputMethod.HOLD);
	}
	
	@Nullable
	public InputMethod getInputMethod() {
		return this.inputMethod.left().orElse(null);
	}
	
}
