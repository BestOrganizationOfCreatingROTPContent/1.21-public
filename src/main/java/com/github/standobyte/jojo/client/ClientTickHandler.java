package com.github.standobyte.jojo.client;

import com.github.standobyte.jojo.client.entitycontrol.ClientEntityController;
import com.github.standobyte.jojo.client.input.ClientsideAim;
import com.github.standobyte.jojo.client.ui.utils.FadeOut;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.mechanics.grab.LivingComponentGrab;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class ClientTickHandler {
	public static int tickCount;

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Pre event) {
		Minecraft mc = Minecraft.getInstance();
		ClientGlobals.tick(mc);
		ClientEntityController.clientTickPre();
		++tickCount;
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

	@SubscribeEvent
	public static void onFrameRender(RenderFrameEvent.Pre event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null) {
			limitEntityRotation(mc.player);
		}
	}

	@SubscribeEvent
	public static void onLivingRender(RenderLivingEvent.Pre<?, ?> event) {
		LivingEntity entity = event.getEntity();
		limitEntityRotation(entity);
	}
	
	protected static void limitEntityRotation(LivingEntity entity) {
		AttachmentType<LivingComponentGrab> attType = ModDataAttachmentTypes.LIVING_GRAB.get();
		if (entity.hasData(attType)) {
			entity.getData(attType).onFrameRender();
		}
	}
	
}
