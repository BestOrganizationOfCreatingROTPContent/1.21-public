package com.github.standobyte.jojo.powersystem.standpower.entity;

import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.EntityActionAbility;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.HeldInput;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.util.StandUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class StandEntityAbility extends EntityActionAbility {
	public boolean noFinisherBarDecay = false;

	public StandEntityAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId);
	}
	
	
	@Override
	public HeldInput onKeyPress(Level level, LivingEntity user, FriendlyByteBuf extraClientInput, 
			InputMethod inputMethod, float clickHoldResolveTime) {
		if (level.isClientSide()) return null;
		
		StandPower power = PowerClass.STAND.get(user); if (power == null) return null;
		StandEntity standEntity = power.getSummonedStandEntity(); if (standEntity == null) return null;
		return setStandAction(this, level, user, 
				power, standEntity, inputMethod, 
				extraClientInput, clickHoldResolveTime);
	}
	
	public static HeldInput setStandAction(StandEntityAbility ability, Level level, LivingEntity user, 
			StandPower power, StandEntity standEntity, InputMethod inputMethod, 
			FriendlyByteBuf extraClientInput, float skipWindupTime) {
		if (level.isClientSide()) return null;

		EntityActionInstance action = ability.initActionOnAbilityUse(level, user, standEntity, extraClientInput);
		HeldInput actionOrQueue = standEntity.getStandActionComponent().bufferOrSetAction(action, user, inputMethod, skipWindupTime);
		return actionOrQueue;
	}
	
	
	@Override
	protected LivingEntity getPerformer(LivingEntity user) {
		return StandUtil.getSummonedStand(user);
	}

}
