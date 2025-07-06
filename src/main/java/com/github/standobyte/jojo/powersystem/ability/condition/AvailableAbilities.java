package com.github.standobyte.jojo.powersystem.ability.condition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.powersystem.Moveset;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;

public class AvailableAbilities {
	public final Map<String, AbilityConditionCheck> inMoveset = new HashMap<>();
	public final Map<String, Ability> inMovesetAndCanBeUsed = new HashMap<>();
	
	public void update(Power<?> context, Moveset baseMoveset) {
		inMoveset.clear();
		
		for (var baseAbilityEntry : baseMoveset.abilities.entrySet()) {
			Ability ability = baseAbilityEntry.getValue();
			ability = ability.replaceWithSubAbility(context);
			if (ability.isAbilityAvailable(context)) {
				AbilityConditionCheck container = getContainerFor(ability);
				inMoveset.put(baseAbilityEntry.getKey(), container);
			}
		}
		
		__visibleIter.clear();
		__visibleIter.addAll(inMoveset.values());
		for (AbilityConditionCheck ability : __visibleIter) {
			ability.ability.onConditionCheck(context, this, ability);
		}
		
		inMovesetAndCanBeUsed.clear();
		for (var abilityEntry : inMoveset.entrySet()) {
			AbilityConditionCheck ability = abilityEntry.getValue();
			if (ability.conditionCheck.isPositive()) {
				inMovesetAndCanBeUsed.put(abilityEntry.getKey(), ability.ability);
			}
		}
	}
	
	
	public void setConditionCheck(String baseAbilityName, ConditionCheck check) {
		AbilityConditionCheck container = inMoveset.get(baseAbilityName);
		if (container != null) {
			container.setConditionCheck(check);
		}
	}

	
	private final Map<AbilityId, AbilityConditionCheck> __cache = new HashMap<>();
	private final Collection<AbilityConditionCheck> __visibleIter = new ArrayList<>();
	
	private AbilityConditionCheck getContainerFor(Ability ability) {
		return __cache.compute(ability.abilityId, (id, existing) -> {
			if (existing == null) return new AbilityConditionCheck(ability);
			else {
				existing.clear();
				return existing;
			}
		});
	}
	
	@ApiStatus.Internal
	public static class AbilityConditionCheck {
		public static final AbilityConditionCheck NULL_ABILITY = new AbilityConditionCheck(null);
		
		public final Ability ability;
		public ConditionCheck conditionCheck;
		
		private AbilityConditionCheck(Ability ability) {
			this.ability = ability;
			this.conditionCheck = ConditionCheck.POSITIVE;
		}
		
		private void clear() {
			this.conditionCheck = ConditionCheck.POSITIVE;
		}
		
		public void setConditionCheck(ConditionCheck check) {
			this.conditionCheck = check;
		}
	}
	
}
