package com.github.standobyte.jojo.jojoimpl.stands._entitybase;

import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.mechanics.grab.LivingComponentGrab;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class StandEntityGrabReleaseAbility extends Ability {

	public StandEntityGrabReleaseAbility(AbilityId abilityId) {
		super(abilityId);
	}
	
	@Override
	public boolean isVisible(LivingEntity user) {
		StandPower standPower = PowerClass.STAND.get(user);
		if (standPower != null) {
			StandEntity standEntity = standPower.getSummonedStandEntity();
			return standEntity != null && LivingComponentGrab.getGrabbedEntity(standEntity) != null;
		}
		
		return false;
	}

	@Override
	public void onClick(Level level, LivingEntity user, 
			FriendlyByteBuf extraClientInput, float clickHoldResolveTime) {
		if (!level.isClientSide()) {
			StandPower standPower = StandPower.get(user);
			if (standPower != null) {
				StandEntity standEntity = standPower.getSummonedStandEntity();
				if (standEntity != null) {
					LivingComponentGrab standGrab = standEntity.getData(ModDataAttachmentTypes.LIVING_GRAB.get());
					if (standGrab != null) {
						standGrab.setGrabbedEntity(null);
					}
				}
			}
		}
	}

}
