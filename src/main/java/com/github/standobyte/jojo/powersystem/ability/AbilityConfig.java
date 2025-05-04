package com.github.standobyte.jojo.powersystem.ability;

import java.util.function.Consumer;

public interface AbilityConfig<A extends Ability> extends Consumer<A> {
	
	default AbilityConfig<A> copy() {
		return this;
	}
	
	default void merge(AbilityConfig<?> edits) {}
}
