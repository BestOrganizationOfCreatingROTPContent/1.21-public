package com.github.standobyte.jojo.mixin.client.standskin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.client.standskin.sound.SoundInstanceWithStandSkin;

import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;

@Mixin(AbstractSoundInstance.class)
public class SoundInstanceMixin implements SoundInstanceWithStandSkin {
	@Shadow protected Sound sound;
	@Shadow @Final protected ResourceLocation location;
	private ResourceLocation jojoROAStandId;
	private Optional<ResourceLocation> jojoROASelectedStandSkin;
	private StandSkin jojoROAStandSkin;
	private StandSkin jojoROADefaultStandSkin;
	
	@Override
	public void jojoROASetStandSkin(ResourceLocation standId, Optional<ResourceLocation> standSkin) {
		this.jojoROAStandId = standId;
		this.jojoROASelectedStandSkin = standSkin;
	}

	@ModifyVariable(method = "resolve", at = @At("STORE"), ordinal = 0)
	public WeighedSoundEvents jojoROAOverrideJsonSound(WeighedSoundEvents soundEvent) {
		if (jojoROAStandId != null) {
			StandSkinsLoader standSkinsLoader = StandSkinsLoader.getInstance();
			jojoROAStandSkin = standSkinsLoader.getSkinFromId(jojoROAStandId, jojoROASelectedStandSkin);
			jojoROADefaultStandSkin = standSkinsLoader.getDefaultSkin(jojoROAStandId);
			if (jojoROAStandSkin == null) {
				jojoROAStandSkin = jojoROADefaultStandSkin;
			}
			WeighedSoundEvents standSkinSoundEvent = jojoROAStandSkin.getSoundEvent(location, jojoROADefaultStandSkin);
			if (standSkinSoundEvent != null) {
				return standSkinSoundEvent;
			}
		}
		
		return soundEvent;
	}
	
	@Inject(method = "resolve", at = @At("TAIL"))
	public void jojoROAOverrideSoundFile(SoundManager handler, CallbackInfoReturnable<WeighedSoundEvents> ci) {
		if (jojoROAStandSkin != null && this.sound != SoundManager.INTENTIONALLY_EMPTY_SOUND) {
			Sound standSkinSound = jojoROAStandSkin.overrideSound(sound, jojoROADefaultStandSkin);
			if (standSkinSound != null) {
				this.sound = standSkinSound;
			}
		}
	}
}
