package com.github.standobyte.jojo.util;

import java.lang.reflect.Field;

import com.github.standobyte.jojo.util.reflection.ReflectionUtil;

import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.neoforged.fml.util.ObfuscationReflectionHelper;

public final class MathUtil {
	public static final float DEG_TO_RAD = (float) (Math.PI / 180D);
	public static final float RAD_TO_DEG = (float) (180D / Math.PI);
	public static final float PI = (float) Math.PI;
	public static final float DOUBLE_PI = PI * 2F;
	
	private static float[] SIN;
	private static final float[] TAN = Util.make(new float[65536], arr -> {
		// FIXME test a built mod (can i really use the MojMap names now Pog ?)
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

}
