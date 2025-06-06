package com.github.standobyte.jojo.client.sound;

import java.util.Optional;

import com.github.standobyte.jojo.client.sound.sounds.EntityLingeringSoundInstance;
import com.github.standobyte.jojo.client.standskin.sound.SoundInstanceWithStandSkin;
import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class ClientsideSoundsHelper {
	
	/**
	 * Call this right before calling {@link net.minecraft.client.sounds.SoundEngine#play(SoundInstance)}
	 * (or when it's abstracted behind something like ClientLevel#playLocalSound or SoundManager#play)
	 */
	public static SoundEvent withStandSkin(SoundEvent soundEvent, ResourceLocation standId, Optional<ResourceLocation> standSkin) {
		ClientsideSoundsHelper.standSkin_soundEvent = soundEvent;
		ClientsideSoundsHelper.standSkin_standId = standId;
		ClientsideSoundsHelper.standSkin_standSkin = standSkin;
		JojoMod.LOGGER.debug("set stand skin for sound {}", soundEvent.location());
		return soundEvent;
	}

	public static void playEntityLingeringSound(Entity entity, SoundEvent sound, SoundSource channel, float volume, float pitch, Level clientLevel) {
		Minecraft.getInstance().getSoundManager().play(new EntityLingeringSoundInstance(
				sound, channel, volume, pitch, entity, clientLevel.random.nextLong()));
	}

	
	// Internal Stand skin handler section
	
	private static SoundEvent standSkin_soundEvent;
	private static ResourceLocation standSkin_standId;
	private static Optional<ResourceLocation> standSkin_standSkin;
	
	@SubscribeEvent
	public static void onSoundPlayed(PlaySoundEvent event) {
		if (standSkin_soundEvent != null) {
			SoundInstance sound = event.getSound();
			if (sound != null && standSkin_soundEvent.location().equals(sound.getLocation())) {
				if (sound instanceof SoundInstanceWithStandSkin withSkin) {
					withSkin.jojo_ripples$setStandSkin(standSkin_standId, standSkin_standSkin);
				}
				JojoMod.LOGGER.debug("consumed stand skin setter for sound {}", standSkin_soundEvent.location());
				standSkin_soundEvent = null;
				standSkin_standId = null;
				standSkin_standSkin = null;
			}
		}
	}

	@SubscribeEvent
	public static void resetJustInCase(ClientTickEvent.Post event) {
		if (standSkin_soundEvent != null) {
			JojoMod.LOGGER.debug("{} didn't trigger", standSkin_soundEvent.location());
			standSkin_soundEvent = null;
			standSkin_standId = null;
			standSkin_standSkin = null;
		}
	}
}
