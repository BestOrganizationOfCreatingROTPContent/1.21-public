package com.github.standobyte.jojo.util;

import java.lang.reflect.Field;
import java.util.Random;

import com.github.standobyte.jojo.util.reflection.ReflectionUtil;

import net.minecraft.Util;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.util.ObfuscationReflectionHelper;

public final class MathUtil {
	public static final float DEG_TO_RAD = (float) (Math.PI / 180D);
	public static final float RAD_TO_DEG = (float) (180D / Math.PI);
	public static final float PI = (float) Math.PI;
	public static final float DOUBLE_PI = PI * 2F;
	
	private static float[] SIN;
	private static final float[] TAN = Util.make(new float[65536], arr -> {
		Field SIN_FIELD = ObfuscationReflectionHelper.findField(Mth.class, "SIN");
		SIN = ReflectionUtil.getFieldValue(SIN_FIELD, null);
		for (int i = 0; i < arr.length; i++) {
			float sin = SIN[i];
			float cos = SIN[(i + 16384) & 65535];
			arr[i] = sin / cos;
		}
	});
	
	public static float tan(float angle) {
		return TAN[(int)(angle * 10430.378F) & 65535];
	}

	public static float wrapRadians(float angle) {
		angle %= DOUBLE_PI;
		if (angle >= PI) {
			angle -= DOUBLE_PI;
		}
		if (angle < -PI) {
			angle += DOUBLE_PI;
		}
		return angle;
	}

	
	public static Vec2 lookAnglesTowards(Vec3 targetPos, Entity lookingEntity, EntityAnchorArgument.Anchor lookingAnchor) {
		Vec3 entityPos = lookingAnchor.apply(lookingEntity);
		Vec3 vecToTarget = targetPos.subtract(entityPos);
		double xzProjLen = Math.sqrt(vecToTarget.x * vecToTarget.x + vecToTarget.z * vecToTarget.z);
		float xRot = Mth.wrapDegrees((float)(-(Mth.atan2(vecToTarget.y, xzProjLen) * RAD_TO_DEG)));
		float yRot = Mth.wrapDegrees((float)(Mth.atan2(vecToTarget.z, vecToTarget.x) * RAD_TO_DEG) - 90);
		return new Vec2(xRot, yRot);
	}

	
	public static double getAABBDistance(AABB aabb1, AABB aabb2) {
		double x1 = Math.min(aabb1.maxX, aabb2.maxX);
		double x2 = Math.max(aabb1.minX, aabb2.minX);
		double xDiff = Math.max(x2 - x1, 0);
		double y1 = Math.min(aabb1.maxY, aabb2.maxY);
		double y2 = Math.max(aabb1.minY, aabb2.minY);
		double yDiff = Math.max(y2 - y1, 0);
		double z1 = Math.min(aabb1.maxZ, aabb2.maxZ);
		double z2 = Math.max(aabb1.minZ, aabb2.minZ);
		double zDiff = Math.max(z2 - z1, 0);
		return Math.sqrt(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff);
	}
	
	
	public static int min(int num1, int num2, int... nums) {
		int min = (num1 <= num2) ? num1 : num2;
		for (int num : nums) {
			if (num < min) {
				min = num;
			}
		}
		return min;
	}
	
	public static float min(float num1, float num2, float... nums) {
		float min = (num1 <= num2) ? num1 : num2;
		for (float num : nums) {
			if (num < min) {
				min = num;
			}
		}
		return min;
	}
	
	public static double min(double num1, double num2, double... nums) {
		double min = (num1 <= num2) ? num1 : num2;
		for (double num : nums) {
			if (num < min) {
				min = num;
			}
		}
		return min;
	}
	
	public static int max(int num1, int num2, int... nums) {
		int max = (num1 >= num2) ? num1 : num2;
		for (int num : nums) {
			if (num > max) {
				max = num;
			}
		}
		return max;
	}
	
	public static float max(float num1, float num2, float... nums) {
		float max = (num1 >= num2) ? num1 : num2;
		for (float num : nums) {
			if (num > max) {
				max = num;
			}
		}
		return max;
	}
	
	public static double max(double num1, double num2, double... nums) {
		double max = (num1 >= num2) ? num1 : num2;
		for (double num : nums) {
			if (num > max) {
				max = num;
			}
		}
		return max;
	}


	public static int fractionRandomInc(double num) {
		int numInt = Mth.floor(num);
		if (Math.random() < num - (double) numInt) {
			numInt++;
		}
		return numInt;
	}

	private static final Random RANDOM = new Random();
	public static int fractionRandomInc(float num) {
		int numInt = Mth.floor(num);
		if (RANDOM.nextFloat() < num - (float) numInt) {
			numInt++;
		}
		return numInt;
	}

	public static int round(double value) {
		int i = (int) value;
		double frac = value > i ? value - i : i - value;
		if (frac < 0.5) {
			return i;
		}
		else {
			return value > i ? i + 1 : i - 1;
		}
	}

}
