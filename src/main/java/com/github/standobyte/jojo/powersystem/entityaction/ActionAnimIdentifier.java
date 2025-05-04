package com.github.standobyte.jojo.powersystem.entityaction;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;

import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.util.StringUtil;
import com.mojang.datafixers.util.Pair;

public class ActionAnimIdentifier {
	public final String name;
	public final int index;

	protected ActionAnimIdentifier(String name) {
		this(name, 0);
	}

	protected ActionAnimIdentifier(String name, int index) {
		this.name = name;
		this.index = index;
	}

	private static final Map<String, ActionAnimIdentifier> ANIM_IDS = new HashMap<>();
	/**
	 * Automatically splits the number at the end of the animation name.
	 */
	public static ActionAnimIdentifier getOrCreate(String animName) {
		Pair<String, OptionalInt> enumeratedName = StringUtil.splitIntAtTheEnd(animName);
		return ANIM_IDS.computeIfAbsent(animName, n -> new ActionAnimIdentifier(enumeratedName.getFirst(), enumeratedName.getSecond().orElse(0)));
	}
	
	public static ActionAnimIdentifier getOrCreate(AbilityId abilityId) {
		return getOrCreate(abilityId, 0);
	}
	
	public static ActionAnimIdentifier getOrCreate(AbilityId abilityId, int animIndex) {
		String abilityName = abilityId.nameInMoveset();
		String name = animIndex > 0 ? abilityName + animIndex : abilityName;
		return ANIM_IDS.computeIfAbsent(name, __ -> new ActionAnimIdentifier(abilityName, animIndex));
	}
	
	@Override
	public String toString() {
		return index > 0 ? name + index : name;
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof ActionAnimIdentifier other && this.name.equals(other.name) && this.index == other.index;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(name, index);
	}
	
}
