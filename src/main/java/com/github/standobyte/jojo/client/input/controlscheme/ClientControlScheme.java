package com.github.standobyte.jojo.client.input.controlscheme;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.PowerType;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities.AbilityConditionCheck;
import com.github.standobyte.jojo.powersystem.ability.controls.ControlSchemeTemplate;
import com.github.standobyte.jojo.powersystem.ability.controls.ControlSchemeTemplate.AbilitiesHotbar;
import com.github.standobyte.jojo.powersystem.ability.controls.InputKey;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Pair;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.settings.KeyModifier;

// TODO (control scheme) ability groups (HUD modes)
//			bind to enable/disable a mode
//				stand hud can't be disabled when the stand is summoned

// TODO (control scheme) hotbar controls
//			switching abilities
//			using abilities

// XXX (control scheme) turn the maps into named classes?
public class ClientControlScheme {
	@ApiStatus.Internal public final Map<String, MoveGroup> moveGroups = new LinkedHashMap<>();
	@ApiStatus.Internal public Map.Entry<String, MoveGroup> curGroup;
	private static final Map.Entry<String, MoveGroup> EMPTY = new AbstractMap.SimpleEntry<>("", new MoveGroup(Component.empty(), null));
	
	public static class MoveGroup {
		@ApiStatus.Internal public final Component name;
		@ApiStatus.Internal public final ClientKeyWrapper toggleHudKey;
		
		@ApiStatus.Internal public final Map<ClientKeyWrapper, Map<InputMethod, BindsByModifier<List<String>>>> binds = new LinkedHashMap<>();
		@ApiStatus.Internal public final List<Hotbar> hotbars = new ArrayList<>();
		
		public MoveGroup(Component name, ClientKeyWrapper toggleHudKey) {
			this.name = name;
			this.toggleHudKey = toggleHudKey;
		}
	}

	public static class Hotbar {
		public final ClientKeyWrapper useAbilityKey;
		public final ClientKeyWrapper switchAbilityKey;
		public final List<Map<InputMethod, BindsByModifier<String>>> slots = new ArrayList<>();
		public int slotIndex = 0;
		
		public Hotbar(ClientKeyWrapper useAbilityKey, ClientKeyWrapper switchAbilityKey) {
			this.useAbilityKey = useAbilityKey;
			this.switchAbilityKey = switchAbilityKey;
		}
	}
	
	public static class BindsByModifier<T> {
		private final T empty;
		private final Map<KeyModifier, T> movesByModifier = new HashMap<>(); // allows null key
		
		public BindsByModifier(T empty) {
			this.empty = empty;
		}
		
		void put(KeyModifier key, T value) {
			movesByModifier.put(key, value);
		}
		
		public T withCurrentModifier(@Nonnull KeyModifier curModifier) {
			if (movesByModifier.containsKey(curModifier)) {
				return movesByModifier.get(curModifier);
			}
			if (movesByModifier.containsKey(null)) {
				return movesByModifier.get(null);
			}
			return empty;
		}
	}
	
	
	
	
	@Nonnull
	public Map.Entry<String, MoveGroup> getCurGroup() {
		if (curGroup == null) {
			curGroup = moveGroups.entrySet().stream().findFirst().orElse(EMPTY);
		}
		return curGroup;
	}
	
	public List<String> getBindsWithModifier(InputMethod keyInputMethod, ClientKeyWrapper key, KeyModifier currentModifier) {
		ClientControlScheme.MoveGroup controls = getCurGroup().getValue();
		var allBindsInKey = controls.binds.get(key);
		if (allBindsInKey != null) {
			var bindsForInputMethod = allBindsInKey.get(keyInputMethod);
			if (bindsForInputMethod != null) {
				return bindsForInputMethod.withCurrentModifier(currentModifier);
			}
		}
		
		return Collections.emptyList();
	}
	
	@Nullable
	public static AbilityConditionCheck prioritizedAbility(List<String> abilityNames, AvailableAbilities available, boolean onlyWithInputActive) {
		return abilityNames.stream()
				.map(abilityName -> available._inMoveset.get(abilityName))
				.filter(a -> a != null && (!onlyWithInputActive || a.ability.cl_IsInputActive().inputActive))
				.sorted(Comparator.comparingInt(a -> a.conditionCheck.isPositive() ? 0 : 1))
				.findFirst().orElse(null);
	}
	
	
	public static ClientControlScheme create(ControlSchemeTemplate template, PowerType powerType) {
		ClientControlScheme controls = new ClientControlScheme();
		
		for (ControlSchemeTemplate.GroupTemplate groupTemplate : template.groups.values()) {
			if (groupTemplate.isEmpty()) continue;
			
			InputKey toggleHudKey = groupTemplate.toggleHudKey;
			if (toggleHudKey == null) {
				PowerClass<?> powerClass = powerType.getPowerClass();
				if (powerClass == PowerClass.STAND) {
					// TODO (control scheme) hud mode keybind
					toggleHudKey = InputKey.K;
				}
				else {
					toggleHudKey = InputKey.J;
				}
			}
			ClientKeyWrapper toggleHudKeybind = getClientKey(toggleHudKey);
			
			ClientControlScheme.MoveGroup group = new ClientControlScheme.MoveGroup(
					Component.translatable(groupTemplate.name), toggleHudKeybind);
			controls.moveGroups.put(groupTemplate.name, group);
			
			// separate binds
			for (Pair<String, Pair<InputMethod, InputKey>> bind : groupTemplate.separateBinds) {
				String abilityName = bind.getFirst();
				Pair<InputMethod, InputKey> input = bind.getSecond();
				InputKey key = input.getSecond();
				
				Map<InputMethod, BindsByModifier<List<String>>> keyAllBinds = group.binds.computeIfAbsent(getClientKey(key), 
						__ -> new EnumMap<>(InputMethod.class));
				var byInputMethod = keyAllBinds.computeIfAbsent(input.getFirst(), 
						__ -> new BindsByModifier<>(Collections.emptyList()));
				List<String> modifierKeyBinds = byInputMethod.movesByModifier.computeIfAbsent(getClientModifier(key.modifier), 
						__ -> new ArrayList<>());
				modifierKeyBinds.add(abilityName);
			}
			
			// ability hotbars
			for (AbilitiesHotbar hotbarTemplate : groupTemplate.hotbars) {
				Hotbar clientHotbar = new Hotbar(
						getClientKey(hotbarTemplate.useAbilityKey), 
						getClientKey(hotbarTemplate.switchAbilityKey));
				for (Map<InputKey.Modifier, Map<InputMethod, String>> slotTemplate : hotbarTemplate.slots) {
					Map<InputMethod, BindsByModifier<String>> slot = new EnumMap<>(InputMethod.class);
					for (var slotVariation : slotTemplate.entrySet()) {
						InputKey.Modifier modifier = slotVariation.getKey();
						for (var abilityEntry : slotVariation.getValue().entrySet()) {
							InputMethod inputMethod = abilityEntry.getKey();
							String ability = abilityEntry.getValue();
							var byInputMethod = slot.computeIfAbsent(inputMethod, 
									__ -> new BindsByModifier<>(""));
							byInputMethod.movesByModifier.put(getClientModifier(modifier), ability);
						}
					}
					clientHotbar.slots.add(slot);
				}
				group.hotbars.add(clientHotbar);
			}
		}
		
		return controls;
	}
	
    public static ClientKeyWrapper getClientKey(InputKey key) {
    	return switch (key.device) {
    		case KEYBOARD -> ClientKeyWrapper.make(InputConstants.Type.KEYSYM, key.keyCode);
    		case MOUSE -> ClientKeyWrapper.make(InputConstants.Type.MOUSE, key.keyCode);
    	};
    }
    
    public static KeyModifier getClientModifier(InputKey.Modifier modifier) {
    	if (modifier == null) return null;
    	return switch (modifier) {
    		case SHIFT -> KeyModifier.SHIFT;
    		case CONTROL -> KeyModifier.CONTROL;
    	};
    }
	
}
