package com.github.standobyte.jojo.util.reflection;

import java.lang.reflect.Field;
import java.util.Map;

import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.neoforged.fml.util.ObfuscationReflectionHelper;

public final class ClientReflection {

	private static final Field SOUND_MANAGER_SOUND_CACHE = ObfuscationReflectionHelper.findField(SoundManager.class, "soundCache");
	public static Map<ResourceLocation, Resource> getSoundCache(SoundManager soundManager) {
		return ReflectionUtil.getFieldValue(SOUND_MANAGER_SOUND_CACHE, soundManager);
	}
	
	private static final Field SOUND_MANAGER_SOUND_ENGINE = ObfuscationReflectionHelper.findField(SoundManager.class, "soundEngine");
	public static SoundEngine getSoundEngine(SoundManager soundManager) {
		return ReflectionUtil.getFieldValue(SOUND_MANAGER_SOUND_ENGINE, soundManager);
	}

	
//	private static final Field GAME_RENDERER_RESOURCE_POOL = ObfuscationReflectionHelper.findField(GameRenderer.class, "resourcePool");
//	public static CrossFrameResourcePool getResourcePool(GameRenderer gameRenderer) {
//		return ReflectionUtil.getFieldValue(GAME_RENDERER_RESOURCE_POOL, gameRenderer);
//	}
	
	
	private static final Field OPTIONS_SCREEN_LAYOUT = ObfuscationReflectionHelper.findField(OptionsScreen.class, "layout");
	public static HeaderAndFooterLayout getLayout(OptionsScreen screen) {
		return ReflectionUtil.getFieldValue(OPTIONS_SCREEN_LAYOUT, screen);
	}
}
