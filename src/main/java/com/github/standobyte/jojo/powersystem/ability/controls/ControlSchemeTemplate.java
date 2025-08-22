package com.github.standobyte.jojo.powersystem.ability.controls;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.Util;

public class ControlSchemeTemplate {
	public Map<String, GroupTemplate> groups = new LinkedHashMap<>();
	public GroupTemplate defaultGroup = new GroupTemplate("moveset_default_group", null);
    private transient Int2ObjectMap<AbilitiesHotbar> hotbarsById = new Int2ObjectArrayMap<>();
    
    public ControlSchemeTemplate deepCopy() {
    	// TODO control scheme deep copy
    	return this;
    }
    
    public static class GroupTemplate {
    	public final String name;
    	@Nullable public final InputKey toggleHudKey;
    	
    	public List<Pair<String, Pair<InputMethod, InputKey>>> separateBinds = new ArrayList<>();
        public List<AbilitiesHotbar> hotbars = new ArrayList<>();
        
        public GroupTemplate(String name, InputKey toggleHudKey) {
        	this.name = name;
        	this.toggleHudKey = toggleHudKey;
        }
        
        public boolean isEmpty() {
        	return separateBinds.isEmpty() && hotbars.isEmpty();
        }
    }
    
    public static class AbilitiesHotbar {
    	public List<Map<InputKey.Modifier, Map<InputMethod, String>>> slots = new ArrayList<>();
    	public InputKey useAbilityKey;
    	public InputKey switchAbilityKey;
    	
    	public AbilitiesHotbar(InputKey useAbilityKey, InputKey switchAbilityKey) {
    		this.useAbilityKey = useAbilityKey;
    		this.switchAbilityKey = switchAbilityKey;
    	}
    }
    
    public ControlSchemeTemplate() {
    	groups.put(defaultGroup.name, defaultGroup);
    }
    
    
    public void makeMovesetGroup(String name, InputKey toggleHudKey) {
    	groups.put(name, new GroupTemplate(name, toggleHudKey));
    }
    
    public GroupTemplate getMovesetGroup(@Nullable String name) {
    	return name == null ? defaultGroup : groups.get(name);
    }
    
	public void bind(String ability, GroupTemplate group, InputKey key, InputMethod inputMethod) {
		if (group != null) {
			group.separateBinds.add(Pair.of(ability, Pair.of(inputMethod, key)));
		}
	}
	
	public void makeHotbar(int hotbarId, GroupTemplate group, InputKey useAbilityKey, InputKey switchAbilityKey) {
		if (group != null) {
			AbilitiesHotbar hotbar = new AbilitiesHotbar(useAbilityKey, switchAbilityKey);
			hotbarsById.put(hotbarId, hotbar);
			group.hotbars.add(hotbar);
		}
	}
	
	public void addToHotbar(String ability, int hotbarId, InputMethod inputMethod) {
		Map<InputKey.Modifier, Map<InputMethod, String>> slot = new HashMap<>();
		slot.put(null, Util.make(new EnumMap<>(InputMethod.class), map -> map.put(inputMethod, ability)));
		hotbarsById.get(hotbarId).slots.add(slot);
	}
	
	public void addHotbarSlotVariation(String ability, String baseAbility, @Nullable InputKey.Modifier modifier, InputMethod inputMethod) {
		for (AbilitiesHotbar hotbar : hotbarsById.values()) {
			for (Map<InputKey.Modifier, Map<InputMethod, String>> slot : hotbar.slots) {
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
