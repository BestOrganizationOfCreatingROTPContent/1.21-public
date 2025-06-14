package com.github.standobyte.jojo.powersystem.standpower.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class StandStatFormulas {

	public static float getHeavyAttackDamage(double strength) {
		float damage = Math.max((float) strength, 1F) * 0.75f;
		return damage;
	}

	public static float getLightAttackDamage(double strength) {
		float damage = Math.max((float) strength * 0.25F, 0.0001F);
		return damage;
	}

	public static float getBarrageHitDamage(double strength) {
		float damage = 0.04F + (float) strength * 0.01F;
		return damage;
	}

	public static int getBarrageHitsPerSecond(double speed) {
		return Math.max((int) (speed * 8.0 - 20.0), 0);
	}

	public static int getBarrageMaxDuration(double durability) {
		return 20 + (int) (durability * 5.0);
	}

	public static float getPhysicalResistance(double durability, double strength, float blocked, float damageDealt) {
		double x = (durability * 2 + strength * 1) / 3;
		double resistance = x / (x + 4); // simplified `1 - 1 / (x / 4 + 1)`
		double dmgCoeff = 1;

		if (blocked > 0) {
			dmgCoeff -= 0.8 * blocked;
			double furtherReductionCap = durability / 2;

			if (damageDealt < furtherReductionCap) {
				dmgCoeff *= damageDealt / furtherReductionCap;
			}
		}

//		double config = JojoModConfig.getCommonConfigInstance(false).standResistanceMultiplier.get();
//		if (config > 1) {
//			dmgCoeff /= config;
//		}

		resistance += (1 - resistance) * Mth.clamp(1 - dmgCoeff, 0, 1);
		return (float) resistance;
	}

	public static float getBlockingKnockbackMult(double durability) {
		return Mth.clamp((float) Math.pow(2, 1 - durability / 4), 0, 1);
	}

	public static float getStaminaMultiplier(double durability) {
		return 1 + (float) durability / 16;
	}

	public static float getBlockStaminaCost(float incomingDamage) {
		return (float) Math.pow(incomingDamage, 2) / 2;
	}

	public static int getBlockingBreakTicks(double durability) {
		return Math.max(240 - (int) (durability * 10), 80);
	}

	public static float getMaxBarrageParryTickDamage(double durability) {
		return Math.max(((float) durability - 4F) * 0.125F, 0);
	}

	public static float getLeapStrength(double strength) {
		return (float) Math.min(strength, 40) / 5F;
	}

	public static float getLeapChargeTime(double speed) {
		float pokaHz = 0;
		return pokaHz;
	}

	public static double getMovementSpeed(double speed) {
		return 0.1 + speed * 0.05;
	}

	public static int leapCooldown(double movementSpeed) {
		return dashCooldown(movementSpeed) * 2 + 5;
	}

	public static int dashCooldown(double movementSpeed) {
		return Math.max((int) (30 - movementSpeed * 25), 2);
	}

	public static float getStandBreakBlockHardness(BlockState blockState, Level level, BlockPos blockPos) {
		float hardness = blockState.getDestroySpeed(level, blockPos);
		if (!blockState.requiresCorrectToolForDrops()) {
			hardness *= 0.6f;
		}
		return hardness;
	}

	public static float rangeStrengthFactor(double rangeEffective, double rangeMax, double distance) {
		if (distance <= rangeEffective || rangeEffective >= rangeMax) {
			return 1F;
		}
		float f = (float) ((rangeMax - rangeEffective) / (2 * rangeEffective - rangeMax - distance));
		return Math.max(f * f, 0.25f);
	}

//	public static double projectileFireRateScaling(StandEntity standEntity, IStandPower standPower) {
//		return standEntity.getAttackSpeed() / standPower.getType().getDefaultStats().getBaseAttackSpeed();
//	}

}
