package com.github.standobyte.jojo.client.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.powersystem.ability.AbilityInput;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities.AbilityConditionCheck;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Pair;

import net.minecraft.Util;
import net.neoforged.neoforge.client.settings.KeyModifier;

public class ControlScheme {
	private final List<Pair<Keybind, String>> binds = new ArrayList<>();
	private final Map<KeybindNoModifier, Keybinds> bindsMap = new LinkedHashMap<>();
	
	public final List<Pair<Keybind, String>> bindsView = Collections.unmodifiableList(binds);
	public final Map<KeybindNoModifier, Keybinds> bindsMapView = Collections.unmodifiableMap(bindsMap);
	
	
	public void setBinds(List<Pair<Keybind, String>> binds) {
		this.binds.clear();
		this.binds.addAll(binds);
		this.bindsMap.clear();
		for (var bind : binds) {
			var keybind = bind.getFirst();
			this.bindsMap
				.computeIfAbsent(keybind.withoutModifier(), __ -> new Keybinds())
				.byModifier.computeIfAbsent(keybind.modifier, __ -> new ArrayList<>())
				.add(bind.getSecond());
		}
	}
	
	
	public List<String> getBindsWithModifier(AbilityInput.InputType keyInputType, Object key, KeyModifier currentModifier) {
		Keybinds keybinds = bindsMap.get(new KeybindNoModifier(keyInputType, key));
		return keybinds != null ? keybinds.withCurrentModifier(currentModifier) : Collections.emptyList();
	}
	
	public static AbilityConditionCheck prioritizedAbility(List<String> abilityNames, AvailableAbilities available, boolean onlyWithInputActive) {
		return abilityNames.stream()
				.map(abilityName -> available.inMoveset.get(abilityName))
				.filter(a -> a != null && (!onlyWithInputActive || a.ability.cl_IsInputActive().inputActive))
				.sorted(Comparator.comparingInt(a -> a.conditionCheck.isPositive() ? 0 : 1))
				.findFirst().orElse(AbilityConditionCheck.NULL_ABILITY);
	}
	
	/**
	 * @param key - for keyboard and mouse controls use {@link com.mojang.blaze3d.platform.InputConstants.Key}
	 */
	public static record Keybind(AbilityInput.InputType inputType, Object key, @Nullable KeyModifier modifier) {
		public KeybindNoModifier withoutModifier() {
			return new KeybindNoModifier(inputType, key);
		}
	}
	
	@ApiStatus.Internal
	public static record KeybindNoModifier(AbilityInput.InputType inputType, Object key) {}
	
	public static class Keybinds {
		private final Map<KeyModifier, List<String>> byModifier = new HashMap<>(); // allows null key
		
		void put(KeyModifier key, List<String> value) {
			byModifier.put(key, value);
		}
		
		public List<String> withCurrentModifier(@Nonnull KeyModifier curModifier) {
			if (byModifier.containsKey(curModifier)) {
				return byModifier.get(curModifier);
			}
			if (byModifier.containsKey(null)) {
				return byModifier.get(null);
			}
			return Collections.emptyList();
		}
	}
	
	
	public static final ControlScheme PROTOTYPE_STAND = Util.make(new ControlScheme(), scheme -> {
		List<Pair<Keybind, String>> binds = new ArrayList<>();
		
		binds.add(Pair.of(new Keybind(AbilityInput.InputType.CLICK, 
				InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_LEFT), null), 
				"punch"));
		
		binds.add(Pair.of(new Keybind(AbilityInput.InputType.HOLD, 
				InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_LEFT), null), 
				"barrage"));
		
		binds.add(Pair.of(new Keybind(AbilityInput.InputType.CLICK, 
				InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_RIGHT), null), 
				"heavy_punch"));
		
		binds.add(Pair.of(new Keybind(AbilityInput.InputType.HOLD, 
				InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_RIGHT), null), 
				"heavy_charged"));
		
		binds.add(Pair.of(new Keybind(AbilityInput.InputType.CLICK, 
				InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_RIGHT), KeyModifier.CONTROL), 
				"grab"));
		
		binds.add(Pair.of(new Keybind(AbilityInput.InputType.CLICK, 
				InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_RIGHT), KeyModifier.CONTROL), 
				"grab_release"));
		
		binds.add(Pair.of(new Keybind(AbilityInput.InputType.HOLD, 
				InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_RIGHT), null), 
				"grabbed_throw"));
		
		binds.add(Pair.of(new Keybind(AbilityInput.InputType.HOLD, 
				InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_C), null), 
				"repair_item"));
		
		scheme.setBinds(binds);
	});
	
	public static final ControlScheme PROTOTYPE_HAMON = Util.make(new ControlScheme(), scheme -> {
		List<Pair<Keybind, String>> binds = new ArrayList<>();
		
		binds.add(Pair.of(new Keybind(AbilityInput.InputType.CLICK, 
				InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_LEFT), null), 
				"hamon_beat"));
		
		binds.add(Pair.of(new Keybind(AbilityInput.InputType.HOLD, 
				InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_LEFT), null), 
				"sunlight_yellow_overdrive"));
		
		binds.add(Pair.of(new Keybind(AbilityInput.InputType.CLICK, 
				InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_RIGHT), null), 
				"rebuff_overdrive"));
		
		scheme.setBinds(binds);
	});
}
