package com.github.standobyte.jojo.client.input;

import java.util.HashMap;
import java.util.Map;

import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityInputHandler.ClickInputType;
import com.github.standobyte.jojo.powersystem.entityaction.EntityAbility;
import com.github.standobyte.jojo.util.entitycomponent.LivingAction;

import net.minecraft.world.entity.LivingEntity;

public class InputBuffer {
	public Map<LivingEntity, InputBufferEntry> bufferPerPerformer = new HashMap<>();
	
	public static record InputBufferEntry(LivingAction performerAction, Power<?> userPower, 
			Ability ability, EntityAbility<?> asEntityAbility, 
			ClickInputType inputType, short heldKeyId) {}
}
