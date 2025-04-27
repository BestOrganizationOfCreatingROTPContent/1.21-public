package com.github.standobyte.jojo.powersystem.ability;

import java.util.function.Consumer;

public interface AbilityConfig<A extends Ability> extends Consumer<A> {
	AbilityConfig<A> copy();
	void merge(AbilityConfig<?> edits);
}
