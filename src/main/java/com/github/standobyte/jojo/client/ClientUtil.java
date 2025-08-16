package com.github.standobyte.jojo.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

public class ClientUtil {

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
