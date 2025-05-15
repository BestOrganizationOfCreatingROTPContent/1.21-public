package com.github.standobyte.jojo.util;

import net.minecraft.world.entity.player.Player;

public class JojoModUtil {

	public static boolean isPlayerSpectator(Player player) {
		return player.isSpectator();
	}
}
