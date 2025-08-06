package com.github.standobyte.jojo.util.reflection;

import java.lang.reflect.Field;
import java.util.Map;

import com.mojang.blaze3d.resource.CrossFrameResourcePool;

import net.minecraft.client.renderer.GameRenderer;
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

	
	private static final Field GAME_RENDERER_RESOURCE_POOL = ObfuscationReflectionHelper.findField(GameRenderer.class, "resourcePool");
	public static CrossFrameResourcePool getResourcePool(GameRenderer gameRenderer) {
		return ReflectionUtil.getFieldValue(GAME_RENDERER_RESOURCE_POOL, gameRenderer);
	}
}
