package com.github.standobyte.jojo.util.reflection;

import java.lang.reflect.Field;
import java.util.Map;

import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.neoforged.fml.util.ObfuscationReflectionHelper;

public final class ClientReflection {

	// FIXME test a built mod (can i really use the MojMap names now Pog ?)
	private static final Field SOUND_MANAGER_SOUND_CACHE = ObfuscationReflectionHelper.findField(SoundManager.class, "soundCache");
	public static Map<ResourceLocation, Resource> getSoundCache(SoundManager soundManager) {
		return ReflectionUtil.getFieldValue(SOUND_MANAGER_SOUND_CACHE, soundManager);
	}
	
	private static final Field SOUND_MANAGER_SOUND_ENGINE = ObfuscationReflectionHelper.findField(SoundManager.class, "soundEngine");
	public static SoundEngine getSoundEngine(SoundManager soundManager) {
		return ReflectionUtil.getFieldValue(SOUND_MANAGER_SOUND_ENGINE, soundManager);
	}
}
