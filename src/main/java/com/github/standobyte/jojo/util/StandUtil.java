package com.github.standobyte.jojo.util;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.core.ModEntityAttributes;
import com.github.standobyte.jojo.mechanics.grab.LivingComponentGrab;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.util.mc.AttributeUtil;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;

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

    public static StandEntity getSummonedStand(Power<?> standPower) {
    	StandPower _standPower = PowerClass.STAND.cast(standPower);
    	return _standPower != null ? _standPower.getSummonedStandEntity() : null;
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
				return LivingComponentGrab.getEntityGrabbedBy(standEntity);
			}
		}
		
		return null;
	}
	
	public static boolean standIgnoresStaminaDebuff(LivingEntity standUser) {
		return ModStatusEffects.isInResolveEffect(standUser);
	}
	
	
	public static double getPhysicalStatValue(StandPower standPower, StandStat stat) {
		StandEntity standEntity = standPower.getSummonedStandEntity();
		LivingEntity user = standPower.getUser();
		if (standEntity != null) {
			return switch (stat) {
				case STRENGTH -> standEntity.getAttackDamage();
				case ATTACK_SPEED -> standEntity.getAttackSpeed();
				case DURABILITY -> standEntity.getDurability();
				case PRECISION -> standEntity.getPrecision();
			};
		}
		else if (user != null) {
			Holder<Attribute> attribute = switch (stat) {
				case STRENGTH -> ModEntityAttributes.STAND_STRENGTH;
				case ATTACK_SPEED -> ModEntityAttributes.STAND_SPEED;
				case DURABILITY -> ModEntityAttributes.STAND_DURABILITY;
				case PRECISION -> ModEntityAttributes.STAND_PRECISION;
			};
			return AttributeUtil.getValueOrDefault(user, attribute, 0);
		}
		
		else return 0;
	}
	
	public enum StandStat {
		STRENGTH,
		ATTACK_SPEED,
		DURABILITY,
		PRECISION
	}
    
}
