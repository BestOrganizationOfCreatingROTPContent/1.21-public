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
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.input.AbilityInputState;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.PowerType;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities.AbilityConditionCheck;
import com.github.standobyte.jojo.powersystem.ability.controls.ControlSchemeTemplate;
import com.github.standobyte.jojo.powersystem.ability.controls.ControlSchemeTemplate.AbilitiesHotbar;
import com.github.standobyte.jojo.powersystem.ability.controls.InputKey;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.util.StandUtil;
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
		
		@ApiStatus.Internal public final Map<ClientKeyWrapper, KeyModifierMap> binds = 
				new TreeMap<>(Comparator.comparingInt(ClientKeyWrapper::keyId));
		@ApiStatus.Internal public final List<Hotbar> hotbars = new ArrayList<>();
		
		public MoveGroup(Component name, ClientKeyWrapper toggleHudKey) {
			this.name = name;
			this.toggleHudKey = toggleHudKey;
		}
	}

	public static class Hotbar {
		public final ClientKeyWrapper useAbilityKey;
		public final ClientKeyWrapper switchAbilityKey;
		public final List<HotbarSlot> slots = new ArrayList<>();
		public int slotIndex = 0;
		
		public Hotbar(ClientKeyWrapper useAbilityKey, ClientKeyWrapper switchAbilityKey) {
			this.useAbilityKey = useAbilityKey;
			this.switchAbilityKey = switchAbilityKey;
		}
		
		@Nullable
		public HotbarSlot getSelected() {
			return this.slotIndex >= 0 && this.slotIndex < this.slots.size() ? this.slots.get(this.slotIndex) : null;
		}
	}
	
	public static class HotbarSlot {
		public final KeyModifierMap binds = new KeyModifierMap();
	}
	
	public static class KeyModifierMap {
		public final Map<KeyModifier, Map<InputMethod, List<PowerClassAbility>>> movesByModifier = new HashMap<>(); // allows null key
		
		public List<PowerClassAbility> getAll(@Nonnull KeyModifier curModifier, InputMethod inputMethod) {
			List<PowerClassAbility> list = null;
			if (movesByModifier.containsKey(curModifier)) {
				list = movesByModifier.get(curModifier).get(inputMethod);
			}
			else if (movesByModifier.containsKey(null)) {
				list = movesByModifier.get(null).get(inputMethod);
			}
			return list != null ? list : Collections.emptyList();
		}
		
		@Nullable
		public PowerClassAbility getFirst(@Nonnull KeyModifier curModifier, InputMethod inputMethod) {
			List<PowerClassAbility> list = getAll(curModifier, inputMethod);
			return !list.isEmpty() ? list.get(0) : null;
		}
	}
	
	public static record PowerClassAbility(PowerClass<?> powerClass, String abilityName) {}
	
	
	
	
	@Nonnull
	public Map.Entry<String, MoveGroup> getCurGroup() {
		if (curGroup == null) {
			curGroup = moveGroups.entrySet().stream().findFirst().orElse(EMPTY);
		}
		return curGroup;
	}
	
	public List<PowerClassAbility> getBindsWithModifier(InputMethod keyInputMethod, ClientKeyWrapper key, KeyModifier currentModifier) {
		ClientControlScheme.MoveGroup controls = getCurGroup().getValue();
		KeyModifierMap allBindsInKey = controls.binds.get(key);
		if (allBindsInKey != null) {
			return allBindsInKey.getAll(currentModifier, keyInputMethod);
		}
		
		for (Hotbar hotbar : controls.hotbars) {
			if (hotbar.useAbilityKey == key) {
				HotbarSlot slot = hotbar.getSelected();
				if (slot != null) {
					return slot.binds.getAll(currentModifier, keyInputMethod);
				}
			}
		}
		
		return Collections.emptyList();
	}
	
	@Nullable
	public static AbilityConditionCheck prioritizedAbility(List<PowerClassAbility> abilityNames, AvailableAbilities available, 
			Power<?> abilityCtx, @Nullable Predicate<AbilityInputState> filter) {
		Stream<AbilityConditionCheck> stream = abilityNames.stream()
				.map(abilityName -> available._inMoveset.get(abilityName.abilityName()))
				.filter(Objects::nonNull);
		if (filter != null) {
			stream = stream.filter(a -> filter.test(AbilityInputState.withValue(a.clientInputState)));
		}
		
		StandEntity standEntity = StandUtil.getSummonedStand(abilityCtx);
		boolean standHoldingItem = standEntity != null && 
				(!standEntity.getMainHandItem().isEmpty() || !standEntity.getOffhandItem().isEmpty());
		
		return stream
				.sorted(Comparator.comparingInt(a -> abilityPriority(a, abilityCtx, standHoldingItem)))
				.findFirst().orElse(null);
	}
	
	protected static int abilityPriority(AbilityConditionCheck ability, Power<?> abilityCtx, boolean standHoldingItem) {
		if (!ability.conditionCheck.isPositive()) {
			return 2;
		}
		return AbilityInputState.withValue(ability.clientInputState).getFlag(AbilityInputState.HIGH_PRIORITY) ? 0 : 1;
	}
	
	
	public static ClientControlScheme create(ControlSchemeTemplate template, PowerType powerType) {
		ClientControlScheme controls = new ClientControlScheme();
		PowerClass<?> powerClass = powerType.getPowerClass();
		
		for (ControlSchemeTemplate.GroupTemplate groupTemplate : template.groups.values()) {
			if (groupTemplate.isEmpty()) continue;
			
			InputKey toggleHudKey = groupTemplate.toggleHudKey;
			if (toggleHudKey == null) {
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
				InputMethod inputMethod = input.getFirst();
				InputKey key = input.getSecond();
				
				KeyModifierMap keyAllBinds = group.binds.computeIfAbsent(getClientKey(key), 
						__ -> new KeyModifierMap());
				Map<InputMethod, List<PowerClassAbility>> modifierKeyBinds = keyAllBinds.movesByModifier.computeIfAbsent(getClientModifier(key.modifier), 
						__ -> new EnumMap<>(InputMethod.class));
				List<PowerClassAbility> byInputMethod = modifierKeyBinds.computeIfAbsent(inputMethod, 
						__ -> new ArrayList<>());
				byInputMethod.add(new PowerClassAbility(powerClass, abilityName));
			}
			
			// ability hotbars
			for (AbilitiesHotbar hotbarTemplate : groupTemplate.hotbars) {
				Hotbar clientHotbar = new Hotbar(
						getClientKey(hotbarTemplate.useAbilityKey), 
						getClientKey(hotbarTemplate.switchAbilityKey));
				for (Map<InputKey.Modifier, Map<InputMethod, String>> slotTemplate : hotbarTemplate.slots) {
					HotbarSlot slot = new HotbarSlot();
					for (var slotVariation : slotTemplate.entrySet()) {
						InputKey.Modifier modifier = slotVariation.getKey();
						for (var abilityEntry : slotVariation.getValue().entrySet()) {
							InputMethod inputMethod = abilityEntry.getKey();
							String ability = abilityEntry.getValue();
							Map<InputMethod, List<PowerClassAbility>> byModifier = slot.binds.movesByModifier.computeIfAbsent(getClientModifier(modifier), 
									__ -> new EnumMap<>(InputMethod.class));
							byModifier.put(inputMethod, 
									Collections.singletonList(new PowerClassAbility(powerClass, ability)));
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
