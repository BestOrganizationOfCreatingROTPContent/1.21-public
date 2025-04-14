package com.github.standobyte.jojo.powersystem.entityaction;

import java.util.HashMap;
import java.util.Map;

import com.github.standobyte.jojo.powersystem.ability.AbilityId;

public class ActionAnimIdentifier {
	public final String name;

	protected ActionAnimIdentifier(String name) {
		this.name = name;
	}

	private static final Map<String, ActionAnimIdentifier> ANIM_IDS = new HashMap<>();
	public static ActionAnimIdentifier getOrCreate(String name) {
		return ANIM_IDS.computeIfAbsent(name, ActionAnimIdentifier::new);
	}
	
	public static ActionAnimIdentifier getOrCreate(AbilityId abilityId) {
		return ANIM_IDS.computeIfAbsent(abilityId.nameInMoveset(), ActionAnimIdentifier::new);
	}
}
