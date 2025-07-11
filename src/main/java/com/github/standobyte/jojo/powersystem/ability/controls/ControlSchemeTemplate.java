package com.github.standobyte.jojo.powersystem.ability.controls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

public class ControlSchemeTemplate {
	public List<Pair<String, AbilityInput>> separateBinds = new ArrayList<>();
    public Int2ObjectMap<AbilitiesHotbar> groups = new Int2ObjectArrayMap<>();
    
    public static record AbilityInput(InputKey key, InputMethod method) {}
    
    public static class AbilitiesHotbar {
    	public List<Map<InputKey.Modifier, Pair<String, InputMethod>>> slots = new ArrayList<>();
    	public InputKey useAbilityKey;
    	public InputKey switchAbilityKey;
    	
    	public AbilitiesHotbar(InputKey useAbilityKey, InputKey switchAbilityKey) {
    		this.useAbilityKey = useAbilityKey;
    		this.switchAbilityKey = switchAbilityKey;
    	}
    }
    
	public void bind(String ability, InputKey key, InputMethod inputMethod) {
		separateBinds.add(Pair.of(ability, new AbilityInput(key, inputMethod)));
	}
	
	public void makeGroup(int hotbarId, InputKey useAbilityKey, InputKey switchAbilityKey) {
		groups.put(hotbarId, new AbilitiesHotbar(useAbilityKey, switchAbilityKey));
	}
	
	public void addToGroup(String ability, int hotbarId, InputMethod inputMethod) {
		Map<InputKey.Modifier, Pair<String, InputMethod>> slot = new HashMap<>();
		slot.put(null, Pair.of(ability, inputMethod));
		groups.get(hotbarId).slots.add(slot);
	}
	
	public void addGroupSlotVariation(String ability, String baseAbility, InputKey.Modifier modifier, InputMethod inputMethod) {
		for (AbilitiesHotbar group : groups.values()) {
			for (var slot : group.slots) {
				Pair<String, InputMethod> baseVariation = slot.get(null);
				if (baseVariation != null && baseAbility.equals(baseVariation.getFirst())) {
					slot.put(modifier, Pair.of(ability, inputMethod));
				}
			}
		}
	}
    
}
