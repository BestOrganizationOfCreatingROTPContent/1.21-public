package com.github.standobyte.jojo.core.datapack;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.standpower.datapack.DataDrivenStandsLoader;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class ModDataPackResources {

	@SubscribeEvent
	public static void addDataPackManagers(AddServerReloadListenersEvent event) {
		event.addListener(JojoMod.resLoc("stands"), DataDrivenStandsLoader.getDatapackStandsLoader());
	}
	
	@SubscribeEvent
	public static void syncDataPack(OnDatapackSyncEvent event) {
		DataDrivenStandsLoader.syncDatapackTo(event.getRelevantPlayers());
	}
	
}
