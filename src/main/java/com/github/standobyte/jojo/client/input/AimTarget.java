package com.github.standobyte.jojo.client.input;

import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.util.StandUtil;
import com.github.standobyte.jojo.util.mc.ActionTarget;

import net.minecraft.client.Minecraft;

public class AimTarget {
	public static ActionTarget playerAimTarget = ActionTarget.EMPTY;
	public static ActionTarget standAimTarget = ActionTarget.EMPTY;
	
	public static void updateTarget(Minecraft mc) {
		playerAimTarget = mc.hitResult != null ? ActionTarget.fromVanilla(mc.hitResult) : ActionTarget.EMPTY;
		
		standAimTarget = ActionTarget.EMPTY;
		if (mc.player != null) {
			StandEntity stand = StandUtil.getSummonedStand(mc.player);
			if (stand != null) {
				standAimTarget = playerAimTarget;
			}
		}
	}

}
