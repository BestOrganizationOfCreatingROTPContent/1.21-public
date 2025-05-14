package com.github.standobyte.jojo.powersystem.ability;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.core.molang.MolangValue;
import com.github.standobyte.jojo.powersystem.entityaction.ActionAnimIdentifier;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.HeldInput;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class EntityActionAbility extends Ability implements EntityActionType {
	protected ActionAnimIdentifier anim;

	public EntityActionAbility(AbilityId abilityId) {
		super(abilityId);
		anim = ActionAnimIdentifier.getOrCreate(abilityId);
	}
	
	
	@Override
	public void onClick(Level level, LivingEntity user) {
		EntityActionInstance action = initActionOnAbilityUse(level, user);
		LivingComponentAction.getComponent(user).setAction(action, false);
	}
	
	@Override
	public HeldInput onButtonStartHold(Level level, LivingEntity user) {
		EntityActionInstance action = initActionOnAbilityUse(level, user);
		LivingComponentAction.getComponent(user).setAction(action, false);
		return action;
	}
	
	
	@ApiStatus.OverrideOnly
	public void initActionFromConfig(EntityActionInstance action, Level level, LivingEntity user) {
		action.phasesLength.put(ActionPhase.WINDUP, windupLength.getAsFloat());
		action.phasesLength.put(ActionPhase.PERFORM, performLength.getAsFloat());
		action.phasesLength.put(ActionPhase.RECOVERY, recoveryLength.getAsFloat());
	}
	
	protected MolangValue windupLength = new MolangValue.Literal(0);
	protected MolangValue performLength = new MolangValue.Literal(1);
	protected MolangValue recoveryLength = new MolangValue.Literal(0);
	
	public void setDefaultPhaseLength(ActionPhase phase, float length) {
		switch (phase) {
			case WINDUP -> windupLength = new MolangValue.Literal(length);
			case PERFORM -> performLength = new MolangValue.Literal(length);
			case RECOVERY -> recoveryLength = new MolangValue.Literal(length);
		}
	}

	@Override
	public ActionAnimIdentifier getEntityAnim(EntityActionInstance action) {
		return anim;
	}

}
