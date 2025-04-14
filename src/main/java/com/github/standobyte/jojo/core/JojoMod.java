package com.github.standobyte.jojo.core;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(JojoMod.MOD_ID)
public class JojoMod {
	public static final String MOD_ID = "jojo_rotp";
	public static final Logger LOGGER = LogUtils.getLogger();

	public JojoMod(IEventBus modEventBus, ModContainer modContainer) {
		modEventBus.register(this);
	}
	
	public static ResourceLocation resLoc(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
	
	@SubscribeEvent
	private void commonSetup(FMLCommonSetupEvent event) {
	}
	
	public static Logger getLogger() {
		return LOGGER;
	}

}
