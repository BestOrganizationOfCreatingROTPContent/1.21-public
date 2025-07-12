package com.github.standobyte.jojo.powersystem.ability.controls;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.Util;

public class ControlSchemeTemplate {
	public List<Pair<String, Pair<InputMethod, InputKey>>> separateBinds = new ArrayList<>();
    public Int2ObjectMap<AbilitiesHotbar> groups = new Int2ObjectArrayMap<>();
    
    public static class AbilitiesHotbar {
    	public List<Map<InputKey.Modifier, Map<InputMethod, String>>> slots = new ArrayList<>();
    	public InputKey useAbilityKey;
    	public InputKey switchAbilityKey;
    	
    	public AbilitiesHotbar(InputKey useAbilityKey, InputKey switchAbilityKey) {
    		this.useAbilityKey = useAbilityKey;
    		this.switchAbilityKey = switchAbilityKey;
    	}
    }
    
    
	public void bind(String ability, InputKey key, InputMethod inputMethod) {
		separateBinds.add(Pair.of(ability, Pair.of(inputMethod, key)));
	}
	
	public void makeGroup(int hotbarId, InputKey useAbilityKey, InputKey switchAbilityKey) {
		groups.put(hotbarId, new AbilitiesHotbar(useAbilityKey, switchAbilityKey));
	}
	
	public void addToGroup(String ability, int hotbarId, InputMethod inputMethod) {
		Map<InputKey.Modifier, Map<InputMethod, String>> slot = new HashMap<>();
		slot.put(null, Util.make(new EnumMap<>(InputMethod.class), map -> map.put(inputMethod, ability)));
		groups.get(hotbarId).slots.add(slot);
	}
	
	public void addGroupSlotVariation(String ability, String baseAbility, @Nullable InputKey.Modifier modifier, InputMethod inputMethod) {
		for (AbilitiesHotbar group : groups.values()) {
			for (Map<InputKey.Modifier, Map<InputMethod, String>> slot : group.slots) {
				Map<InputMethod, String> baseVariation = slot.get(null);
				if (baseVariation != null && baseVariation.values().contains(baseAbility)) {
					Map<InputMethod, String> byInputMethod = slot.computeIfAbsent(modifier, 
							__ -> new EnumMap<>(InputMethod.class));
					byInputMethod.put(inputMethod, ability);
				}
			}
		}
	}
    
}
