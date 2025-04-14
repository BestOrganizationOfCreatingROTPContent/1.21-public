package com.github.standobyte.jojo.client;

import com.github.standobyte.jojo.client.entityanim.AnimationLoader;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.client.utils.ResourcePathChecker;
import com.github.standobyte.jojo.core.JojoMod;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModClientResources {

	@SubscribeEvent
	public static void registerResourceLoaders(AddClientReloadListenersEvent event) {
		event.addListener(JojoMod.resLoc("resource_check"), new ResourcePathChecker.ResourceReloadNotifier());
		StandSkinsLoader.init(event);
		AnimationLoader.init(event);
	}
}
