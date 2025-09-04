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
	public boolean isIdle = false;

	protected ActionAnimIdentifier(String name) {
		this(name, 0);
	}

	protected ActionAnimIdentifier(String name, int index) {
		this.name = name;
		this.index = index;
	}
	
	public ActionAnimIdentifier setIdle(boolean isIdle) {
		this.isIdle = isIdle;
		return this;
	}

	private static final Map<String, ActionAnimIdentifier> ANIM_IDS = new HashMap<>();
	/**
	 * Automatically splits the number at the end of the animation name.
	 */
	public static ActionAnimIdentifier getOrCreate(String animName, boolean setIdle) {
		Pair<String, OptionalInt> enumeratedName = StringUtil.splitIntAtTheEnd(animName);
		ActionAnimIdentifier anim = ANIM_IDS.computeIfAbsent(animName, n -> new ActionAnimIdentifier(
				enumeratedName.getFirst(), 
				enumeratedName.getSecond().orElse(1) - 1 /* 1-based indexing in anims */));
		if (setIdle) {
			anim.isIdle = true;
		}
		return anim;
	}
	
	public static ActionAnimIdentifier getOrCreate(AbilityId abilityId) {
		return getOrCreate(abilityId.nameInMoveset(), false);
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
