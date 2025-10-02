package com.github.standobyte.jojo.modcompat;

import java.util.HashMap;
import java.util.Map;

import net.neoforged.fml.ModList;

public class ModInteractionUtil {
	private static Map<String, Boolean> checked = new HashMap<>();
	
	public static boolean isModLoaded(String modId) {
		return checked.computeIfAbsent(modId, ModList.get()::isLoaded);
	}
	
	public static void clientTickPre() {
		JojoModsInteraction.WingsOfRequiem._cacheWoRClientPlayerData();
	}
}
