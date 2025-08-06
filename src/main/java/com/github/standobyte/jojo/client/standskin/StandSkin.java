package com.github.standobyte.jojo.client.standskin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;

import com.github.standobyte.jojo.client.entityanim.AnimWithExtras;
import com.github.standobyte.jojo.client.entityanim.AnimationSet;
import com.github.standobyte.jojo.client.entityrender.stand.StandEntityModel;
import com.github.standobyte.jojo.client.entityrender.stand.StandEntityRenderState;
import com.github.standobyte.jojo.client.entityrender.stand.StandEntityRenderer;
import com.github.standobyte.jojo.client.standskin.sound.CustomPathSound;
import com.github.standobyte.jojo.client.utils.ResourcePathChecker;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class StandSkin {
	public final ResourceLocation skinId;
	public final ResourceLocation standTypeId;
	protected final boolean isDefault;
	protected final ResourcePathChecker standTexture;
	protected final OptionalInt color;
	
	protected Map<ResourceLocation, LayerDefinition> models = new HashMap<>();
	protected LayerDefinition standModel;
	protected Map<ResourceLocation, Optional<Model>> createdModelsCache = new HashMap<>();
	protected Optional<StandEntityModel<?, ?>> createdStandModelCache;
	
	protected Map<ResourceLocation, AnimationSet> animations = new HashMap<>();
	protected AnimationSet standEntityAnims;
	
	protected Map<ResourceLocation, WeighedSoundEvents> soundEvents = new HashMap<>();
	protected Map<ResourceLocation, ResourceLocation> existingSounds = new HashMap<>();
	protected Map<ResourceLocation, Sound> remappedSound = new HashMap<>();
	
	protected final Map<ResourceLocation, ResourcePathChecker> remapPathCache = new HashMap<>();
	
	public StandSkin(ResourceLocation skinId, ResourceLocation standId, OptionalInt color) {
		this.skinId = skinId;
		this.standTypeId = standId;
		this.standTexture = remapAssetPath(ResourceLocation.fromNamespaceAndPath(
				standId.getNamespace(), 
				"textures/entity/" + standId.getPath() + ".png"));
		this.isDefault = skinId.equals(standId);
		this.color = color;
	}
	
	protected void withModels(Map<ResourceLocation, LayerDefinition> models) {
		Objects.requireNonNull(models);
		this.models = models;
		this.standModel = models.get(standTypeId);
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

	
	@Deprecated
	public int getColor() {
		return this.color.orElse(0xffffffff);
	}
	
	public int getColor(StandSkin defaultSkin) {
		if (this.color.isPresent()) {
			return this.color.getAsInt();
		}
		if (this != defaultSkin && defaultSkin.color.isPresent()) {
			return defaultSkin.color.getAsInt();
		}
		return 0xffffff;
	}
	
	public ResourceLocation getTexture(ResourceLocation path, StandSkin defaultSkin, ResourceLocation defaultTex) {
		ResourcePathChecker remapped = remapAssetPath(path);
		if (this != defaultSkin && defaultSkin != null) {
			return remapped.or(() -> defaultSkin.getTexture(path, defaultSkin, defaultTex));
		}
		else {
			return remapped.or(defaultTex);
		}
	}
	
	public ResourceLocation getStandTexture(StandSkin defaultSkin, ResourceLocation defaultTex) {
		if (this != defaultSkin && defaultSkin != null) {
			return this.standTexture.or(() -> defaultSkin.getStandTexture(defaultSkin, defaultTex));
		}
		else {
			return this.standTexture.or(defaultTex);
		}
	}
	
	public Model getModel(ResourceLocation modelPath, StandEntityRenderer<?, ?, ?> entityRenderer, StandSkin defaultSkin) {
		Optional<Model> cached = createdModelsCache.get(modelPath);
		if (cached != null) {
			return cached.orElse(null);
		}
		LayerDefinition modelDefinition = models.get(modelPath);
		if (modelDefinition != null) {
			cached = Optional.ofNullable(entityRenderer.createModel(modelPath, modelDefinition));
			createdModelsCache.put(modelPath, cached);
			return cached.orElse(null);
		}
		
		if (this != defaultSkin) {
			return defaultSkin.getModel(modelPath, entityRenderer, defaultSkin);
		}
		
		return null;
	}
	
	public 
		<T extends StandEntity, 
		S extends StandEntityRenderState, 
		M extends StandEntityModel<T, S>> 
	M getStandModel(
			StandEntityRenderer<T, S, M> entityRenderer, StandSkin defaultSkin) {
		if (this.createdStandModelCache != null) {
			return (M) createdStandModelCache.orElse(null);
		}
		if (this.standModel != null) {
			this.createdStandModelCache = Optional.ofNullable(entityRenderer.createStandModel(standModel));
			return (M) this.createdStandModelCache.orElse(null);
		}
		
		if (this != defaultSkin) {
			return defaultSkin.getStandModel(entityRenderer, defaultSkin);
		}
		
		return null;
	}
	
	public LayerDefinition getModelDef(ResourceLocation modelId, StandSkin defaultSkin) {
		LayerDefinition model = models.get(modelId);
		if (model != null || this == defaultSkin) {
			return model;
		}
		
		if (defaultSkin != null) {
			return defaultSkin.getModelDef(modelId, defaultSkin);
		}
		return null;
	}
	
	public LayerDefinition getStandModelDef(StandSkin defaultSkin) {
		if (standModel != null) {
			return standModel;
		}

		if (defaultSkin != null) {
			return defaultSkin.standModel;
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
