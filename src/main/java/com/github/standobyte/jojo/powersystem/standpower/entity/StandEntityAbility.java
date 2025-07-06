package com.github.standobyte.jojo.powersystem.standpower.entity;

import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityInput.InputType;
import com.github.standobyte.jojo.powersystem.ability.EntityActionAbility;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.HeldInput;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class StandEntityAbility extends EntityActionAbility {

	public StandEntityAbility(AbilityId abilityId) {
		super(abilityId);
	}
	
	
	@Override
	public void onClick(Level level, LivingEntity user, 
			FriendlyByteBuf extraClientInput, float clickHoldResolveTime) {
		if (level.isClientSide()) return;
		
		StandPower power = PowerClass.STAND.get(user); if (power == null) return;
		StandEntity standEntity = power.getSummonedStandEntity(); if (standEntity == null) return;
		setStandAction(this, level, user, 
				power, standEntity, InputType.CLICK, 
				extraClientInput, clickHoldResolveTime);
	}
	
	@Override
	public HeldInput onButtonStartHold(Level level, LivingEntity user, 
			FriendlyByteBuf extraClientInput, float clickHoldResolveTime) {
		if (level.isClientSide()) return null;
		
		StandPower power = PowerClass.STAND.get(user); if (power == null) return null;
		StandEntity standEntity = power.getSummonedStandEntity(); if (standEntity == null) return null;
		return setStandAction(this, level, user, 
				power, standEntity, InputType.HOLD, 
				extraClientInput, clickHoldResolveTime);
	}
	
	public static HeldInput setStandAction(StandEntityAbility ability, Level level, LivingEntity user, 
			StandPower power, StandEntity standEntity, InputType inputType, 
			FriendlyByteBuf extraClientInput, float skipWindupTime) {
		if (level.isClientSide()) return null;
		
		EntityActionInstance action = ability.initActionOnAbilityUse(level, user, extraClientInput);
		HeldInput actionOrQueue = standEntity.getStandActionComponent().bufferOrSetAction(action, user, inputType, skipWindupTime);
		return actionOrQueue;
	}

}
