package com.github.standobyte.jojo.client.hud;

import com.github.standobyte.jojo.client.utils.RGBUtil;

public class ElementTransparency extends FadeOut {
	
	ElementTransparency(int ticksMax, int ticksStartFadeOut) {
		super(ticksMax, ticksStartFadeOut);
	}
	
	boolean shouldRender() {
		return ticks > 0;
	}
	
	int makeTextColorTranclucent(int color, float partialTick) {
		return RGBUtil.addAlpha(color, getAlpha(partialTick));
	}
	
	public static final float MIN_ALPHA = 1F / 63F;
	float getAlpha(float partialTick) {
		return ticks > 0 ? Math.max(getValue(partialTick), MIN_ALPHA) : 0;
	}
}
