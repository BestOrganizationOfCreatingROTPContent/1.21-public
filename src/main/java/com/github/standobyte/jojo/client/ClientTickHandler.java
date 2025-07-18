package com.github.standobyte.jojo.client;

import com.github.standobyte.jojo.client.entitycontrol.ClientEntityController;
import com.github.standobyte.jojo.client.input.ClientsideAim;
import com.github.standobyte.jojo.client.ui.utils.FadeOut;
import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class ClientTickHandler {

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Pre event) {
		Minecraft mc = Minecraft.getInstance();
		ClientGlobals.tick(mc);
		ClientEntityController.clientTickPre();
	}

	@SubscribeEvent
	public static void onClientTickPost(ClientTickEvent.Post event) {
		Minecraft mc = Minecraft.getInstance();
		ClientsideAim.updateTarget(mc, 1);
		ClientsideAim.updateTargetWithServer(mc);
		ClientEntityController.clientTickPost();
		if (!mc.isPaused()) {
			for (var fadeOut : FadeOut.__TO_TICK) fadeOut.__tick();
		}
	}
}
