package com.github.standobyte.jojo.util;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.mechanics.grab.LivingComponentGrab;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;

import net.minecraft.world.entity.LivingEntity;

public class StandUtil {

    public static LivingEntity getStandUser(LivingEntity entityMaybeStand) {
        if (entityMaybeStand instanceof StandEntity stand) {
            LivingEntity user = stand.getUser();
            if (user != null) return user;
        }
        return entityMaybeStand;
    }
    
    @Nullable
    public static StandEntity getSummonedStand(LivingEntity standUser) {
    	StandPower standPower = StandPower.get(standUser);
    	return standPower != null ? standPower.getSummonedStandEntity() : null;
    }
    
    public static boolean isEntityStandUser(LivingEntity entity) {
    	StandPower standData = StandPower.get(entity);
    	return standData != null ? standData.hasPower() : false;
    }
	
	@Nullable
	public static LivingEntity getStandGrabTarget(Power<?> power) {
		StandPower standPower = PowerClass.STAND.cast(power);
		if (standPower != null) {
			StandEntity standEntity = standPower.getSummonedStandEntity();
			if (standEntity != null) {
				return LivingComponentGrab.getGrabbedEntity(standEntity);
			}
		}
		
		return null;
	}
    
}
