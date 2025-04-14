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
	private ResourceLocation rotpStandId;
	private Optional<ResourceLocation> rotpSelectedStandSkin;
	private StandSkin rotpStandSkin;
	private StandSkin rotpDefaultStandSkin;
	
	@Override
	public void setStandSkin(ResourceLocation standId, Optional<ResourceLocation> standSkin) {
		this.rotpStandId = standId;
		this.rotpSelectedStandSkin = standSkin;
	}

	@ModifyVariable(method = "resolve", at = @At("STORE"), ordinal = 0)
	public WeighedSoundEvents rotpOverrideJsonSound(WeighedSoundEvents soundEvent) {
		if (rotpStandId != null) {
			StandSkinsLoader standSkinsLoader = StandSkinsLoader.getInstance();
			rotpStandSkin = standSkinsLoader.getSkinFromId(rotpStandId, rotpSelectedStandSkin);
			rotpDefaultStandSkin = standSkinsLoader.getDefaultSkin(rotpStandId);
			if (rotpStandSkin == null) {
				rotpStandSkin = rotpDefaultStandSkin;
			}
			WeighedSoundEvents standSkinSoundEvent = rotpStandSkin.getSoundEvent(location, rotpDefaultStandSkin);
			if (standSkinSoundEvent != null) {
				return standSkinSoundEvent;
			}
		}
		
		return soundEvent;
	}
	
	@Inject(method = "resolve", at = @At("TAIL"))
	public void rotpOverrideSoundFile(SoundManager handler, CallbackInfoReturnable<WeighedSoundEvents> ci) {
		if (rotpStandSkin != null && this.sound != SoundManager.INTENTIONALLY_EMPTY_SOUND) {
			Sound standSkinSound = rotpStandSkin.overrideSound(sound, rotpDefaultStandSkin);
			if (standSkinSound != null) {
				this.sound = standSkinSound;
			}
		}
	}
}
