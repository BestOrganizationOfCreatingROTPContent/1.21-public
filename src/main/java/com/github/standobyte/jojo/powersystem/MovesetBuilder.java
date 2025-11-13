package com.github.standobyte.jojo.powersystem;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.init.power.ModStandAbilities;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.config.ConfigAbilityFactory;
import com.github.standobyte.jojo.powersystem.ability.controls.InputBindTemplate;
import com.github.standobyte.jojo.powersystem.ability.controls.ControlSchemeTemplate;
import com.github.standobyte.jojo.powersystem.ability.controls.InputKey;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.github.standobyte.jojo.powersystem.ability.controls.InputUseVanillaMapping;
import com.github.standobyte.jojo.powersystem.skill.UnlockableSkill;

import net.minecraft.resources.ResourceLocation;

@ApiStatus.NonExtendable
public class MovesetBuilder {
	public final Map<String, ConfigAbilityFactory<?>> abilities = new LinkedHashMap<>();
	public final Map<String, UnlockableSkill> unlockableSkills = new LinkedHashMap<>();
	@ApiStatus.Internal
	public ControlSchemeTemplate _controlScheme = new ControlSchemeTemplate();
//	protected final Set<String> disable = new HashSet<>();
	
	
	public MovesetBuilder deepCopy() {
		MovesetBuilder copy = new MovesetBuilder();
		for (var abilityEntry : abilities.entrySet()) {
			copy.abilities.put(abilityEntry.getKey(), abilityEntry.getValue().copy());
		}
		copy.unlockableSkills.putAll(this.unlockableSkills);
		copy._controlScheme = this._controlScheme.deepCopy();
		return copy;
	}
	
	public Moveset build(PowerClass<?> powerClass, ResourceLocation powerTypeId) {
		Map<String, Ability> abilities = new LinkedHashMap<>();
		for (var abilityEntry : this.abilities.entrySet()) {
			String abilityName = abilityEntry.getKey();
			var abilityFactory = abilityEntry.getValue();
//			if (!disable.contains(key)) {
				var ability = abilityFactory.makeAbility(new AbilityId(powerClass, powerTypeId, abilityName));
				abilities.put(abilityName, ability);
//			}
		}
		Moveset moveset = new Moveset(abilities, powerClass, powerTypeId);
		moveset.controlScheme = this._controlScheme;
		return moveset;
	}
	
	
	public <A extends Ability> MovesetBuilder addAbility(String abilityName, AbilityType<A> abilityType) {
		return addAbility(abilityName, abilityType, null);
	}

	public final <A extends Ability> MovesetBuilder addAbility(String abilityName, AbilityType<A> abilityType, 
			@Nullable Consumer<A> init) {
		abilities.put(abilityName, new ConfigAbilityFactory<>(abilityType, init));
		lastAbility = abilityName;
		return this;
	}
	
	public <A extends Ability> MovesetBuilder addAbility(String abilityName, Supplier<? extends AbilityType<A>> abilityType) {
		return addAbility(abilityName, abilityType.get());
	}
	
	public final <A extends Ability> MovesetBuilder addAbility(String abilityName, Supplier<? extends AbilityType<A>> abilityType, 
			Consumer<A> init) {
		return addAbility(abilityName, abilityType.get(), init);
	}
	
	
//	public MovesetBuilder disableAbility(String abilityName) {
//		disable.add(abilityName);
//		return this;
//	}

	
	// shortcuts for some common abilities
	
	public MovesetBuilder addHumanoidStandStuff() {
		addManualControl();
		addItemUsage();
		return this;
	}
	
	public MovesetBuilder addManualControl() {
		addAbility("manual_control", ModStandAbilities.MANUAL_CONTROL)
		.withBind(InputMethod.CLICK, InputKey.O);
		
		return this;
	}
	
	public MovesetBuilder addItemUsage() {
		addAbility("items_swap_w_user", ModStandAbilities.ITEMS_SWAP_W_USER)
		.withBind(InputMethod.CLICK, InputKey.F.withModifier(InputKey.Modifier.CONTROL));

		addAbility("items_swap_hands", ModStandAbilities.ITEMS_SWAP_HANDS)
		.withBind(InputMethod.CLICK, new InputUseVanillaMapping("key.swapOffhand"));

		addAbility("item_toss", ModStandAbilities.ITEM_TOSS)
		.withBind(InputMethod.CLICK, new InputUseVanillaMapping("key.drop"));
		
		return this;
	}
	
	
	// Control scheme stuff
	
	protected String lastAbility;
	
	public MovesetBuilder makeMovesetGroup(String name, InputBindTemplate toggleHudKey) {
		_controlScheme.makeMovesetGroup(name, toggleHudKey);
		return this;
	}
	
	public MovesetBuilder withBind(InputMethod inputMethod, InputBindTemplate key) {
		return withBind(null, inputMethod, key);
	}

	public MovesetBuilder withBind(String movesetGroupName, InputMethod inputMethod, InputBindTemplate key) {
		var group = _controlScheme.getMovesetGroup(movesetGroupName);
		_controlScheme.bind(lastAbility, group, key, inputMethod);
		return this;
	}
	
	public MovesetBuilder makeHotbar(int hotbarId, InputBindTemplate useAbilityKey, InputBindTemplate switchAbilityKey) {
		return makeHotbar(null, hotbarId, useAbilityKey, switchAbilityKey);
	}
	
	public MovesetBuilder makeHotbar(String movesetGroupName, int hotbarId, InputBindTemplate useAbilityKey, InputBindTemplate switchAbilityKey) {
		var group = _controlScheme.getMovesetGroup(movesetGroupName);
		_controlScheme.makeHotbar(hotbarId, group, useAbilityKey, switchAbilityKey);
		return this;
	}
	
	public MovesetBuilder inHotbar(int hotbarId, InputMethod inputMethod) {
		_controlScheme.addToHotbar(lastAbility, hotbarId, inputMethod);
		return this;
	}
	
	public MovesetBuilder inHotbarSlotVariation(String baseAbility, @Nullable InputKey.Modifier modifier, InputMethod inputMethod) {
		_controlScheme.addHotbarSlotVariation(lastAbility, baseAbility, modifier, inputMethod);
		return this;
	}
	
	// Unlockable skill stuff
	
	public MovesetBuilder addSkill(UnlockableSkill skill) {
		unlockableSkills.put(skill.skillName, skill);
		return this;
	}
	
	
	
	/**
	 * @deprecated Use {@link #withBind(InputMethod, InputBindTemplate)} (just swap the parameters)
	 */
	public MovesetBuilder withBind(InputBindTemplate key, InputMethod inputMethod) {
		return withBind(inputMethod, key);
	}

	/**
	 * @deprecated. Use {@link #withBind(String, InputMethod, InputBindTemplate)}  (just swap the 2nd and 3rd parameters)
	 */
	@Deprecated
	public MovesetBuilder withBind(String movesetGroupName, InputBindTemplate key, InputMethod inputMethod) {
		return withBind(movesetGroupName, inputMethod, key);
	}
	
}
