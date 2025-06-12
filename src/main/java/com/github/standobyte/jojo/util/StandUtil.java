package com.github.standobyte.jojo.util;

import javax.annotation.Nullable;

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
    
}
