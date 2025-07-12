package com.github.standobyte.jojo.powersystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.Nullable;

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
	protected final ControlSchemeTemplate controlScheme = new ControlSchemeTemplate();
	protected final Set<String> disable = new HashSet<>();
	

	public <A extends Ability> MovesetBuilder addAbility(String abilityName, AbilityType<A> abilityType) {
		return addAbility(abilityName, abilityType, true);
	}

	@SafeVarargs
	public final <A extends Ability> MovesetBuilder addAbility(String abilityName, AbilityType<A> abilityType, boolean isBaseMove, 
			AbilityConfigComponent<A>... setParameters) {
		abilities.put(abilityName, new ConfigAbilityFactory<>(abilityType, setParameters));
		lastAbility = abilityName;
		return this;
	}
	
	
	public <A extends Ability> MovesetBuilder addAbility(String abilityName, Supplier<? extends AbilityType<A>> abilityType) {
		return addAbility(abilityName, abilityType.get());
	}
	
	@SafeVarargs
	public final <A extends Ability> MovesetBuilder addAbility(String abilityName, Supplier<? extends AbilityType<A>> abilityType, boolean isBaseMove, 
			AbilityConfigComponent<A>... setParameters) {
		return addAbility(abilityName, abilityType.get(), isBaseMove, setParameters);
	}
	
	
	protected String lastAbility;
	public MovesetBuilder bind(InputKey key, InputMethod inputMethod) {
		controlScheme.bind(lastAbility, key, inputMethod);
		return this;
	}
	
	public MovesetBuilder makeGroup(int hotbarId, InputKey useAbilityKey, InputKey switchAbilityKey) {
		controlScheme.makeGroup(hotbarId, useAbilityKey, switchAbilityKey);
		return this;
	}
	
	public MovesetBuilder addToGroup(int hotbarId, InputMethod inputMethod) {
		controlScheme.addToGroup(lastAbility, hotbarId, inputMethod);
		return this;
	}
	
	public MovesetBuilder addGroupSlotVariation(String baseAbility, @Nullable InputKey.Modifier modifier, InputMethod inputMethod) {
		controlScheme.addGroupSlotVariation(lastAbility, baseAbility, modifier, inputMethod);
		return this;
	}
	
	
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
