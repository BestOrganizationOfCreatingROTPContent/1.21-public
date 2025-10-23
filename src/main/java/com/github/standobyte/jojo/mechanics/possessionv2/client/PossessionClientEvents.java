package com.github.standobyte.jojo.mechanics.possessionv2.client;

import com.github.standobyte.jojo.mechanics.possessionv2.LivingComponentPossession;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

@EventBusSubscriber
public class PossessionClientEvents {
	
	@SubscribeEvent
	public static void cancelEntityRender(RenderLivingEvent.Pre<?, ?> event) {
		if (LivingComponentPossession.isPossessingSomeone(event.getEntity())) {
			event.setCanceled(true);
		}
	}
}
