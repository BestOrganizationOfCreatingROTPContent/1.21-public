package com.github.standobyte.jojo.powersystem;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.config.ConfigAbilityFactory;
import com.github.standobyte.jojo.powersystem.ability.controls.ControlSchemeTemplate;

import net.minecraft.resources.ResourceLocation;

@ApiStatus.NonExtendable
public class Moveset {
	@ApiStatus.Internal public final Map<String, Ability> abilities;
	@ApiStatus.Internal public ControlSchemeTemplate controlScheme = new ControlSchemeTemplate();
	
	public Moveset(Stream<Map.Entry<String, ConfigAbilityFactory<?>>> abilities, PowerClass<?> powerClass, ResourceLocation powerTypeId) {
		this.abilities = abilities.collect(Collectors.toMap(
				Map.Entry::getKey, entry -> entry.getValue().makeAbility(new AbilityId(powerClass, powerTypeId, entry.getKey()))));
	}
	
	public Ability getAbility(String name) {
		return abilities.get(name);
	}
	
	
	protected static final Moveset EMPTY = new Moveset(Stream.empty(), null, null);
	
	public static Moveset empty() {
		return EMPTY;
	}
	
}
