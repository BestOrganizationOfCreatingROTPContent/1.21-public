package com.github.standobyte.jojo.jojoimpl.stands._entitybase;

import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.client.entitycontrol.ClientEntityController;
import com.github.standobyte.jojo.client.entitycontrol.stand.ClientStandController;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.util.StandUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class StandEntityManualControlToggle extends Ability {

	public StandEntityManualControlToggle(AbilityId abilityId) {
		super(abilityId);
	}
	
	@Override
	public void onClick(Level level, LivingEntity user, 
			FriendlyByteBuf extraClientInput, float clickHoldResolveTime) {
		StandEntity stand = StandUtil.getSummonedStand(user);
		if (stand != null) {
			if (!stand.isManuallyControlled()) {
				on(level, stand);
			}
			else {
				off(level, stand);
			}
		}
	}
	
	public static void on(Level level, StandEntity stand) {
		stand.setManuallyControlled(true);
		if (level.isClientSide() && stand.getUser() == ClientProxy.getClientPlayer()) {
			ClientEntityController.setInstance(new ClientStandController(stand));
		}
	}
	
	public static void off(Level level, StandEntity stand) {
		stand.setManuallyControlled(false);
		if (level.isClientSide() && stand.getUser() == ClientProxy.getClientPlayer()) {
			ClientEntityController.setInstance(null);
		}
	}

}
