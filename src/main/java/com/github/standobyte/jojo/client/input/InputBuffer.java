package com.github.standobyte.jojo.client.input;

import java.util.HashMap;
import java.util.Map;

import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityInputHandler.ClickInputType;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;

import net.minecraft.world.entity.LivingEntity;

public class InputBuffer {
	public Map<LivingEntity, InputBufferEntry> bufferPerPerformer = new HashMap<>();
	
	public static record InputBufferEntry(LivingComponentAction performerAction, Power<?> userPower, 
			Ability entityAbility, ClickInputType inputType, short heldKeyId) {}
}
