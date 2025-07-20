package com.github.standobyte.jojo.powersystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.power.ModStandAbilities;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.config.AbilityConfigComponent;
import com.github.standobyte.jojo.powersystem.ability.config.ConfigAbilityFactory;
import com.github.standobyte.jojo.powersystem.ability.controls.ControlSchemeTemplate;
import com.github.standobyte.jojo.powersystem.ability.controls.InputKey;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;

public class MovesetBuilder {
	protected final Map<String, ConfigAbilityFactory<?>> abilities = new HashMap<>();
	protected ControlSchemeTemplate controlScheme = new ControlSchemeTemplate();
	protected final Set<String> disable = new HashSet<>();
	
	@SuppressWarnings("unchecked")
	public <A extends Ability> MovesetBuilder addAbility(String abilityName, AbilityType<A> abilityType) {
		return addAbility(abilityName, abilityType, new AbilityConfigComponent[0]);
	}

	@SafeVarargs
	public final <A extends Ability> MovesetBuilder addAbility(String abilityName, AbilityType<A> abilityType, 
			AbilityConfigComponent<A>... setParameters) {
		abilities.put(abilityName, new ConfigAbilityFactory<>(abilityType, setParameters));
		lastAbility = abilityName;
		return this;
	}
	
	
	public <A extends Ability> MovesetBuilder addAbility(String abilityName, Supplier<? extends AbilityType<A>> abilityType) {
		return addAbility(abilityName, abilityType.get());
	}
	
	@SafeVarargs
	public final <A extends Ability> MovesetBuilder addAbility(String abilityName, Supplier<? extends AbilityType<A>> abilityType, 
			AbilityConfigComponent<A>... setParameters) {
		return addAbility(abilityName, abilityType.get(), setParameters);
	}

	
	// shortcuts for some common abilities
	
	public MovesetBuilder addHumanoidStandStuff() {
		addManualControl();
		addItemUsage();
		addBlockUsage();
		return this;
	}
	
	public MovesetBuilder addManualControl() {
		addAbility("manual_control", ModStandAbilities.MANUAL_CONTROL)
		.withBind(InputKey.O, InputMethod.CLICK);
		
		return this;
	}
	
	public MovesetBuilder addItemUsage() {
		addAbility("items_swap_w_user", ModStandAbilities.ITEMS_SWAP_W_USER)
		.withBind(InputKey.F.withModifier(InputKey.Modifier.CONTROL), InputMethod.CLICK);
		
		addAbility("items_swap_hands", ModStandAbilities.ITEMS_SWAP_HANDS)
		.withBind(InputKey.F, InputMethod.CLICK);
		
		addAbility("item_toss", ModStandAbilities.ITEM_TOSS)
		.withBind(InputKey.Q, InputMethod.CLICK);
		
		addAbility("item_attack", ModStandAbilities.ITEM_ATTACK)
		.withBind(InputKey.LMB, InputMethod.HOLD);
		
		addAbility("item_use", ModStandAbilities.ITEM_USE)
		.withBind(InputKey.RMB, InputMethod.HOLD);
		
		return this;
	}
	
	public MovesetBuilder addBlockUsage() {
		addAbility("block_use", ModStandAbilities.BLOCK_USE)
		.withBind(InputKey.RMB, InputMethod.CLICK);
		
		return this;
	}
	
	
	// Control scheme stuff here
	
	protected String lastAbility;
	
	public MovesetBuilder makeMovesetGroup(String name, InputKey toggleHudKey) {
		controlScheme.makeMovesetGroup(name, toggleHudKey);
		return this;
	}
	
	public MovesetBuilder withBind(InputKey key, InputMethod inputMethod) {
		return withBind(null, key, inputMethod);
	}
	
	public MovesetBuilder withBind(String movesetGroupName, InputKey key, InputMethod inputMethod) {
		var group = controlScheme.getMovesetGroup(movesetGroupName);
		controlScheme.bind(lastAbility, group, key, inputMethod);
		return this;
	}
	
	public MovesetBuilder makeHotbar(int hotbarId, InputKey useAbilityKey, InputKey switchAbilityKey) {
		return makeHotbar(null, hotbarId, useAbilityKey, switchAbilityKey);
	}
	
	public MovesetBuilder makeHotbar(String movesetGroupName, int hotbarId, InputKey useAbilityKey, InputKey switchAbilityKey) {
		var group = controlScheme.getMovesetGroup(movesetGroupName);
		controlScheme.makeHotbar(hotbarId, group, useAbilityKey, switchAbilityKey);
		return this;
	}
	
	public MovesetBuilder inHotbar(int hotbarId, InputMethod inputMethod) {
		controlScheme.addToHotbar(lastAbility, hotbarId, inputMethod);
		return this;
	}
	
	public MovesetBuilder inHotbarSlotVariation(String baseAbility, @Nullable InputKey.Modifier modifier, InputMethod inputMethod) {
		controlScheme.addHotbarSlotVariation(lastAbility, baseAbility, modifier, inputMethod);
		return this;
	}
	
	// Control scheme stuff over
	
	
	public MovesetBuilder disableAbility(String abilityName) {
		disable.add(abilityName);
		return this;
	}
	
	
	public Moveset build(PowerClass<?> powerClass, ResourceLocation powerTypeId) {
		var abilities = this.abilities.entrySet().stream()
				.filter(ability -> !disable.contains(ability.getKey()));
		Moveset moveset = new Moveset(abilities, powerClass, powerTypeId);
		moveset.controlScheme = this.controlScheme;
		return moveset;
	}
	
	
	public static Codec<MovesetBuilder> codec() {
		return BUILDER_CODEC;
	}
	
	// XXX deserialize default control schemes
	protected static final Codec<MovesetBuilder> BUILDER_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.simpleMap(Codec.STRING, ConfigAbilityFactory.CODEC, null).codec().fieldOf("abilities").forGetter(moveset -> moveset.abilities), 
			Codec.list(Codec.STRING).fieldOf("disable").forGetter(moveset -> new ArrayList<>(moveset.disable))
			)
			.apply(instance, (Map<String, ConfigAbilityFactory<?>> abilities, List<String> disable) -> {
				MovesetBuilder moveset = new MovesetBuilder();
				for (var ability : abilities.entrySet()) {
					moveset.abilities.put(ability.getKey(), ability.getValue());
				}
				for (var disableAbility : disable) {
					moveset.disableAbility(disableAbility);
				}
				return moveset;
			}));
	
}
