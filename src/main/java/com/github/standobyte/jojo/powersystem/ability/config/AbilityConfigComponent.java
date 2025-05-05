package com.github.standobyte.jojo.powersystem.ability.config;

import java.util.function.Consumer;

import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.mojang.serialization.Codec;

public interface AbilityConfigComponent<A extends Ability> extends Consumer<A> {
	default boolean isSerialized() { return false; }
	default String getFieldName() { throw new UnsupportedOperationException(); }
	default Codec<? extends AbilityConfigComponent<A>> getCodec() { throw new UnsupportedOperationException(); }
}
