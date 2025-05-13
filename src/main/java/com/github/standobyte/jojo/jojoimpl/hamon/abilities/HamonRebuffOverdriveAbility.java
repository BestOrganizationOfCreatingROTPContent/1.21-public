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

public class HamonRebuffOverdriveAbility extends Ability implements EntityActionAbility {
	protected ActionAnimIdentifier anim;

	public HamonRebuffOverdriveAbility(AbilityId abilityId) {
		super(abilityId);
		setDefaultPhaseLength(ActionPhase.WINDUP, 14);
		setDefaultPhaseLength(ActionPhase.PERFORM, 10);
		setDefaultPhaseLength(ActionPhase.RECOVERY, 16);
		anim = ActionAnimIdentifier.getOrCreate("rebuff_overdrive");
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
		return new HamonRebuffOverdrive(this);
	}

	public static class HamonRebuffOverdrive extends EntityActionInstance {
		
		public HamonRebuffOverdrive(EntityActionAbility ability) {
			super(ability);
		}

		@Override
		public void actionPerform() {
			JojoMod.LOGGER.debug("REBUFF OVERDRIVE");
		}
	}
	
}
