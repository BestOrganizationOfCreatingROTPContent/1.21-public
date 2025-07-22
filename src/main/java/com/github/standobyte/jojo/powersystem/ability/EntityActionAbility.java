package com.github.standobyte.jojo.powersystem.ability;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.core.molang.MolangValue;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.github.standobyte.jojo.powersystem.entityaction.ActionAnimIdentifier;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.HeldInput;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class EntityActionAbility extends Ability implements EntityActionType {
	protected ActionAnimIdentifier anim;

	public EntityActionAbility(AbilityId abilityId) {
		super(abilityId);
		anim = ActionAnimIdentifier.getOrCreate(abilityId);
	}
	
	
	@Override
	public HeldInput onKeyPress(Level level, LivingEntity user, FriendlyByteBuf extraClientInput, 
			InputMethod inputMethod, float clickHoldResolveTime) {
		if (level.isClientSide()) return null;

		EntityActionInstance action = initActionOnAbilityUse(level, user, extraClientInput);
		HeldInput actionOrQueue = LivingComponentAction.getComponent(user)
				.bufferOrSetAction(action, user, inputMethod, clickHoldResolveTime);
		return actionOrQueue;
	}
	
	
	/**
	 * Is used to initialize some EntityActionInstance values, 
	 * that are either configurable (stuff like phases length), 
	 * or are context-dependent.
	 */
	@ApiStatus.OverrideOnly
	public void initActionFromConfig(EntityActionInstance action, Level level, LivingEntity user) {
		var map = action.phasesLength;
		if (!map.containsKey(ActionPhase.BUTTON_CHARGE))	map.put(ActionPhase.BUTTON_CHARGE,	buttonChargePhase.getAsFloat());
		if (!map.containsKey(ActionPhase.WINDUP))			map.put(ActionPhase.WINDUP,			windupPhase.getAsFloat());
		if (!map.containsKey(ActionPhase.PERFORM))			map.put(ActionPhase.PERFORM,		performPhase.getAsFloat());
		if (!map.containsKey(ActionPhase.RECOVERY))			map.put(ActionPhase.RECOVERY,		recoveryPhase.getAsFloat());
	}

	protected MolangValue buttonChargePhase = new MolangValue.Literal(0);
	protected MolangValue windupPhase = new MolangValue.Literal(0);
	protected MolangValue performPhase = new MolangValue.Literal(1);
	protected MolangValue recoveryPhase = new MolangValue.Literal(0);
	
	public void setDefaultPhaseLength(ActionPhase phase, float length) {
		switch (phase) {
			case BUTTON_CHARGE -> buttonChargePhase = new MolangValue.Literal(length);
			case WINDUP -> windupPhase = new MolangValue.Literal(length);
			case PERFORM -> performPhase = new MolangValue.Literal(length);
			case RECOVERY -> recoveryPhase = new MolangValue.Literal(length);
		}
	}

	@Override
	public ActionAnimIdentifier getEntityAnim(EntityActionInstance action) {
		return anim;
	}

}
