package com.github.standobyte.jojo.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

public class ClientUtil {
	public static final int MAX_LIGHT = 0xF000F0;

	public static void setCameraEntityPreventShaderSwitch(Entity entity) {
		Minecraft mc = Minecraft.getInstance();
		mc.setCameraEntity(entity);
		// XXX prevent shader switch
//		if (mc.gameRenderer.currentEffect() == null) {
//			ShaderEffectApplier.getInstance().updateCurrentShader();
//		}
	}
	
	public static float partialTick(DeltaTracker fuckThis, boolean worksInPauseToo) {
		return fuckThis.getGameTimeDeltaPartialTick(worksInPauseToo);
	}
}
