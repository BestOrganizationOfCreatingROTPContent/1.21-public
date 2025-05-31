package com.github.standobyte.jojo.client.utils;

import net.minecraft.util.ARGB;

public class RGBUtil {
	
	public static float[] argb(int color) {
		return new float[] {
				ARGB.alpha(color) / 255F, 
				ARGB.red(color) / 255F, 
				ARGB.green(color) / 255F, 
				ARGB.blue(color) / 255F
		};
	}
	
	public static int[] argbInt(int color) {
		return new int[] {
				ARGB.alpha(color), 
				ARGB.red(color), 
				ARGB.green(color), 
				ARGB.blue(color)
		};
	}
	
//	public static int discColor(int color) {
//		return (((0xFFFFFF - color) & 0xFEFEFE) >> 1) + color;
//	}
	
	public static int addAlpha(int color, float alpha) {
		return color | ((int) (255F * alpha)) << 24 & -0x1000000;
	}

	public static int scaleAlpha(int argbColor, float alphaScale) {
		return ARGB.color(
				Math.clamp(((int) (ARGB.alpha(argbColor) * alphaScale)), 0, 255),
				ARGB.red(argbColor),
				ARGB.green(argbColor),
				ARGB.blue(argbColor)
				);
	}
}
