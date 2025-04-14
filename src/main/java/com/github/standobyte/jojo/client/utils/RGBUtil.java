package com.github.standobyte.jojo.client.utils;

// XXX net.minecraft.util.ARGB class might already have some of these
public class RGBUtil {
	
	public static float[] rgb(int color) {
		int[] rgbInt = rgbInt(color);
		return new float[] {
				(float) rgbInt[0] / 255F,
				(float) rgbInt[1] / 255F,
				(float) rgbInt[2] / 255F
		};
	}
	
	public static int[] rgbInt(int color) {
		int red = (color >> 16) & 0xFF;
		int green = (color >> 8) & 0xFF;
		int blue = color & 0xFF;
		return new int[] {red, green, blue};
	}
	
	public static int fromRgb(float r, float g, float b) {
		return ((int) (r * 255) << 16) + ((int) (g * 255) << 8) + (int) (b * 255);
	}
	
	public static int fromRgbInt(int r, int g, int b) {
		return (r << 16) + (g << 8) + b;
	}
	
	public static int discColor(int color) {
		return (((0xFFFFFF - color) & 0xFEFEFE) >> 1) + color;
	}
	
	public static int addAlpha(int color, float alpha) {
		return color | ((int) (255F * alpha)) << 24 & -0x1000000;
	}
}
