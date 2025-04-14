package com.github.standobyte.jojo.powersystem.standpower.type;

import java.util.Optional;

import com.github.standobyte.jojo.powersystem.standpower.StandPower;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public interface SummonedStand {
	void setUserAndPower(LivingEntity user, StandPower power);
	void tickStand(LivingEntity user, StandPower userStand);
	default void setSelectedSkin(Optional<ResourceLocation> skin) {}
	
	public static class BlankSummonedStand implements SummonedStand {
		protected LivingEntity user;
		protected StandPower power;
		
		@Override
		public void setUserAndPower(LivingEntity user, StandPower power) {
			this.user = user;
			this.power = power;
		}

		@Override
		public void tickStand(LivingEntity user, StandPower userStand) {}
		
	}
}
