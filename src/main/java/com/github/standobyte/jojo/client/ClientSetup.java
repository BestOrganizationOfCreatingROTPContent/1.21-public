package com.github.standobyte.jojo.client;

import com.github.standobyte.jojo.client.entityrender.stand.StandEntityRenderer;
import com.github.standobyte.jojo.client.input.InputHandler;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.ModEntityTypes;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onClientSetup0(FMLClientSetupEvent event) {
	}

	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
//		Minecraft mc = Minecraft.getInstance();
	}
	
	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(ModEntityTypes.HUMANOID_STAND.get(), StandEntityRenderer::new);
	}
	
//	@SubscribeEvent
//	public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
//		
//	}
//	
//	@SubscribeEvent
//	public static void addLayers(EntityRenderersEvent.AddLayers event) {
//		
//	}
	
	@SubscribeEvent
	public static void registerKeyBindings(RegisterKeyMappingsEvent event) {
		InputHandler.init(event);
	}
}
