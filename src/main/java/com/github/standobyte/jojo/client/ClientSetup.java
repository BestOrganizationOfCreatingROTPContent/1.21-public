package com.github.standobyte.jojo.client;

import com.github.standobyte.jojo.client.entitycontrol.stand.StandHudElements;
import com.github.standobyte.jojo.client.input.InputHandler;
import com.github.standobyte.jojo.client.ui.hud.marker.MarkerRenderer;
import com.github.standobyte.jojo.client.ui.hud.marker.StandAimMarker;
import com.github.standobyte.jojo.client.ui.jojomenu.JojoMenuTabs;
import com.github.standobyte.jojo.core.JojoMod;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onClientSetup0(FMLClientSetupEvent event) {
		JojoMenuTabs.initDefaults();
	}

	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
//		Minecraft mc = Minecraft.getInstance();
		registerMarkers();
		StandHudElements.init();
	}
	
	private static void registerMarkers() {
		MarkerRenderer.registerMarkerRenderer(new StandAimMarker());
	}
	
	@SubscribeEvent
	public static void registerKeyBindings(RegisterKeyMappingsEvent event) {
		InputHandler.init(event);
	}
}
