package com.github.standobyte.jojo.jojoimpl.hamon.abilities;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.entityaction.ActionAnimIdentifier;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionAbility;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.util.entitycomponent.LivingAction;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class HamonOverdriveBeatAbility extends Ability implements EntityActionAbility {
	protected ActionAnimIdentifier anim;

	public HamonOverdriveBeatAbility(AbilityId abilityId) {
		super(abilityId);
		setDefaultPhaseLength(ActionPhase.WINDUP, 5);
		setDefaultPhaseLength(ActionPhase.PERFORM, 3);
		setDefaultPhaseLength(ActionPhase.RECOVERY, 2);
		anim = ActionAnimIdentifier.getOrCreate(abilityId.nameInMoveset());
	}

	@Override
	public ActionAnimIdentifier getEntityAnim(EntityActionInstance action) {
		return anim;
	}
	
	@Override
	public void onClick(Level level, LivingEntity user) {
		EntityActionInstance rebuffOverdrive = initActionOnAbilityUse(level, user);
		LivingAction.getComponent(user).setAction(rebuffOverdrive, false);
	}
	
	@Override
	public EntityActionInstance createActionObj() {
		return new HamonOverdriveBeat(this);
	}

	public static class HamonOverdriveBeat extends EntityActionInstance {
		
		public HamonOverdriveBeat(EntityActionAbility ability) {
			super(ability);
		}

		@Override
		public void actionPerform() {
			JojoMod.LOGGER.debug("НЫА");
		}
	}
	
}
