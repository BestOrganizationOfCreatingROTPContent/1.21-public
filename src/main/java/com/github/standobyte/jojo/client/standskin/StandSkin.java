package com.github.standobyte.jojo.client.standskin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import com.github.standobyte.jojo.client.entityanim.AnimWithExtras;
import com.github.standobyte.jojo.client.entityanim.AnimationSet;
import com.github.standobyte.jojo.client.entityrender.stand.StandEntityModel;
import com.github.standobyte.jojo.client.standskin.sound.CustomPathSound;
import com.github.standobyte.jojo.client.utils.ResourcePathChecker;

import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.Lazy;

public class StandSkin {
	public final ResourceLocation skinId;
	public final ResourceLocation standTypeId;
	private final boolean isDefault;
	private final ResourcePathChecker standTexture;
	
	private Map<ResourceLocation, LayerDefinition> models = new HashMap<>();
	private Lazy<StandEntityModel<?>> standModel;
	
	private Map<ResourceLocation, AnimationSet> animations = new HashMap<>();
	private AnimationSet standEntityAnims;
	
	private Map<ResourceLocation, WeighedSoundEvents> soundEvents = new HashMap<>();
	private Map<ResourceLocation, ResourceLocation> existingSounds = new HashMap<>();
	private Map<ResourceLocation, Sound> remappedSound = new HashMap<>();
	
	private final Map<ResourceLocation, ResourcePathChecker> remapPathCache = new HashMap<>();
	
	public StandSkin(ResourceLocation skinId, ResourceLocation standId) {
		this.skinId = skinId;
		this.standTypeId = standId;
		this.standTexture = remapAssetPath(ResourceLocation.fromNamespaceAndPath(
				standId.getNamespace(), 
				"textures/entity/" + standId.getPath() + ".png"));
		this.isDefault = skinId.equals(standId);
	}
	
	protected void withModels(Map<ResourceLocation, LayerDefinition> models) {
		Objects.requireNonNull(models);
		this.models = models;
		LayerDefinition standModel = models.get(standTypeId);
		this.standModel = standModel != null ? Lazy.of(() -> new StandEntityModel<>(standModel.bakeRoot())) : null;
	}
	
	protected void withAnimations(Map<ResourceLocation, AnimationSet.Builder> animations) {
		Objects.requireNonNull(animations);
		this.animations = new HashMap<>();
		this.standEntityAnims = null;
		animations.forEach((key, animsBuilder) -> {
			if (!animsBuilder.isEmpty()) {
				AnimationSet anims = animsBuilder.build();
				this.animations.put(key, anims);
				if (key.equals(standTypeId)) {
					this.standEntityAnims = anims;
				}
			}
		});
	}
	
	protected void withSoundEvents(Map<ResourceLocation, WeighedSoundEvents> soundEvents) {
		this.soundEvents = soundEvents;
	}
	
	protected void withSounds(Map<ResourceLocation, ResourceLocation> sounds) {
		this.existingSounds = sounds;
	}
	
	
	public Optional<ResourceLocation> getNonDefaultId() {
		return isDefault ? Optional.empty() : Optional.of(skinId);
	}

	
	public ResourceLocation getTexture(ResourceLocation path, StandSkin defaultSkin) {
		ResourcePathChecker remapped = remapAssetPath(path);
		if (this == defaultSkin) {
			return remapped.path;
		}
		
		if (defaultSkin != null) {
			return remapped.or(defaultSkin.getTexture(path, defaultSkin));
		}
		return null;
	}
	
	public ResourceLocation getStandTexture(StandSkin defaultSkin) {
		if (this == defaultSkin) {
			return standTexture.path;
		}
		
		if (defaultSkin != null) {
			return standTexture.or(defaultSkin.standTexture.path);
		}
		return null;
	}
	
	public LayerDefinition getModel(ResourceLocation modelId, StandSkin defaultSkin) {
		LayerDefinition model = models.get(modelId);
		if (model != null || this == defaultSkin) {
			return model;
		}
		
		if (defaultSkin != null) {
			return defaultSkin.getModel(modelId, defaultSkin);
		}
		return null;
	}
	
	public StandEntityModel<?> getStandModel(StandSkin defaultSkin) {
		if (standModel != null) {
			return standModel.get();
		}
		
		if (defaultSkin != null && defaultSkin.standModel != null) {
			return defaultSkin.standModel.get();
		}
		return null;
	}
	
	public AnimWithExtras getAnimation(ResourceLocation modelId, Function<AnimationSet, AnimWithExtras> getAnim, StandSkin defaultSkin) {
		AnimationSet anims = this.animations.get(modelId);
		if (anims != null || this == defaultSkin) {
			AnimWithExtras anim = getAnim.apply(anims);
			if (anim != null) {
				return anim;
			}
		}
		
		if (defaultSkin != null) {
			return defaultSkin.getAnimation(modelId, getAnim, defaultSkin);
		}
		return null;
	}
	
	public AnimWithExtras getStandAnimation(Function<AnimationSet, AnimWithExtras> getAnim, StandSkin defaultSkin) {
		if (this.standEntityAnims != null) {
			AnimWithExtras anim = getAnim.apply(this.standEntityAnims);
			if (anim != null) {
				return anim;
			}
		}
		
		if (defaultSkin != null && defaultSkin.standEntityAnims != null) {
			return getAnim.apply(defaultSkin.standEntityAnims);
		}
		return null;
	}
	
//	public WeighedSoundEvents getSoundEvent(SoundEvent soundEvent, StandSkin defaultSkin) {
//		return getSoundEvent(soundEvent.location(), defaultSkin);
//	}
	
	public WeighedSoundEvents getSoundEvent(ResourceLocation soundEventLocation, StandSkin defaultSkin) {
		WeighedSoundEvents sound = this.soundEvents.get(soundEventLocation);
		if (sound != null) {
			return sound;
		}
		
		if (this != defaultSkin) {
			return defaultSkin.getSoundEvent(soundEventLocation, defaultSkin);
		}
		return null;
	}
	
	public Sound overrideSound(Sound sound, StandSkin defaultSkin) {
		ResourceLocation key = sound.getLocation();
		Sound cached = remappedSound.get(key);
		if (cached != null) {
			return cached;
		}
		ResourceLocation path = existingSounds.get(key);
		if (path != null) {
			return remappedSound.compute(key, (__, ___) -> new CustomPathSound(sound, path));
		}
		
		if (this != defaultSkin) {
			return defaultSkin.overrideSound(sound, defaultSkin);
		}
		return null;
	}
	
	
	public ResourcePathChecker remapAssetPath(ResourceLocation path) {
		return remapPathCache.computeIfAbsent(path, asset -> ResourcePathChecker.getOrCreate(StandSkinsLoader.remap(asset, skinId)));
	}
	
	
	public MutableComponent translatable(String key) {
		return Component.translatable(key);
	}

	public MutableComponent translatable(String key, Object... args) {
		return Component.translatable(key, args);
	}
	
}
