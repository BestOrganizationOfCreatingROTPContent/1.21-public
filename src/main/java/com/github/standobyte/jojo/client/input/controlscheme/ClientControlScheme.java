package com.github.standobyte.jojo.client.input.controlscheme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities.AbilityConditionCheck;
import com.github.standobyte.jojo.powersystem.ability.controls.ControlSchemeTemplate;
import com.github.standobyte.jojo.powersystem.ability.controls.ControlSchemeTemplate.AbilityInput;
import com.github.standobyte.jojo.powersystem.ability.controls.InputKey;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Pair;

import net.neoforged.neoforge.client.settings.KeyModifier;

public class ClientControlScheme {
	private final Map<BaseKey, BindsByModifier> binds = new LinkedHashMap<>();
	public final Map<BaseKey, BindsByModifier> bindsView = Collections.unmodifiableMap(binds);
	
	@ApiStatus.Internal
	public static record BaseKey(InputMethod inputMethod, ClientKeyWrapper key) {}
	
	public static class BindsByModifier {
		private final Map<KeyModifier, List<String>> movesByModifier = new HashMap<>(); // allows null key
		
		void put(KeyModifier key, List<String> value) {
			movesByModifier.put(key, value);
		}
		
		public List<String> withCurrentModifier(@Nonnull KeyModifier curModifier) {
			if (movesByModifier.containsKey(curModifier)) {
				return movesByModifier.get(curModifier);
			}
			if (movesByModifier.containsKey(null)) {
				return movesByModifier.get(null);
			}
			return Collections.emptyList();
		}
	}
	
	
	public List<String> getBindsWithModifier(InputMethod keyInputMethod, ClientKeyWrapper key, KeyModifier currentModifier) {
		BindsByModifier keybinds = binds.get(new BaseKey(keyInputMethod, key));
		return keybinds != null ? keybinds.withCurrentModifier(currentModifier) : Collections.emptyList();
	}
	
	public static AbilityConditionCheck prioritizedAbility(List<String> abilityNames, AvailableAbilities available, boolean onlyWithInputActive) {
		return abilityNames.stream()
				.map(abilityName -> available._inMoveset.get(abilityName))
				.filter(a -> a != null && (!onlyWithInputActive || a.ability.cl_IsInputActive().inputActive))
				.sorted(Comparator.comparingInt(a -> a.conditionCheck.isPositive() ? 0 : 1))
				.findFirst().orElse(AbilityConditionCheck.NULL_ABILITY);
	}
	
	
	public static ClientControlScheme create(ControlSchemeTemplate template) {
		ClientControlScheme controls = new ClientControlScheme();
		
		for (Pair<String, AbilityInput> bind : template.separateBinds) {
			String abilityName = bind.getFirst();
			AbilityInput input = bind.getSecond();
			InputKey key = input.key();
			
			BindsByModifier keyBinds = controls.binds.computeIfAbsent(new BaseKey(input.method(), getClientKey(key)), __ -> new BindsByModifier());
			List<String> modifierKeyBinds = keyBinds.movesByModifier.computeIfAbsent(getClientModifier(key), __ -> new ArrayList<>());
			modifierKeyBinds.add(abilityName);
		}
		
		// TODO add special ability hotbar
		
		return controls;
	}
	
    public static ClientKeyWrapper getClientKey(InputKey key) {
    	return switch (key.device) {
    		case KEYBOARD -> ClientKeyWrapper.make(InputConstants.Type.KEYSYM, key.keyCode);
    		case MOUSE -> ClientKeyWrapper.make(InputConstants.Type.MOUSE, key.keyCode);
    	};
    }
    
    public static KeyModifier getClientModifier(InputKey key) {
    	if (key.modifier == null) return null;
    	return switch (key.modifier) {
    		case SHIFT -> KeyModifier.SHIFT;
    		case CONTROL -> KeyModifier.CONTROL;
    	};
    }
	
}
