package com.github.standobyte.jojo.client;

import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.util.JojoModUtil;

import net.minecraft.client.Minecraft;

public class ClientGlobals {
	public static boolean canSeeStands;
	public static boolean canHearStands;
	
	public static void tick(Minecraft mc) {
		if (mc.player != null) {
			if (JojoModUtil.isPlayerSpectator(mc.player)) {
				canSeeStands = true;
			}
			else {
				StandPower stand = StandPower.get(mc.player);
				canSeeStands = stand != null && stand.hasPower();
			}
			canHearStands = canSeeStands;
		}
	}
	
}
