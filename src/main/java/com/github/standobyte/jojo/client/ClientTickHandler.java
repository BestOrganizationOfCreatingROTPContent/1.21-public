package com.github.standobyte.jojo.client;

import com.github.standobyte.jojo.core.JojoMod;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class ClientTickHandler {

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Pre event) {
//		Minecraft mc = Minecraft.getInstance();
	}
}
