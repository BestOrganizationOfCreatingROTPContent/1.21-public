package com.github.standobyte.jojo.client.standskin;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

import com.github.standobyte.jojo.client.entityanim.AnimationLoader;
import com.github.standobyte.jojo.client.entityanim.AnimationSet;
import com.github.standobyte.jojo.client.entityrender.parsemodel.ParseModEntityModel;
import com.github.standobyte.jojo.client.entityrender.parsemodel.ParseModEntityModel.Format;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader.StandSkinResourceBuilder;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.standpower.StandInstance;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;
import com.github.standobyte.jojo.util.JSONUtil;
import com.github.standobyte.jojo.util.StringUtil;
import com.github.standobyte.jojo.util.reflection.ClientReflection;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundEventRegistration;
import net.minecraft.client.resources.sounds.SoundEventRegistrationSerializer;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.client.sounds.Weighted;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.Zone;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.resources.VanillaClientListeners;

public class StandSkinsLoader extends SimplePreparableReloadListener<Map<ResourceLocation, StandSkinResourceBuilder>> {
	private static StandSkinsLoader instance;
	
	@ApiStatus.Internal
	public static void init(AddClientReloadListenersEvent event) {
		if (instance == null) {
			instance = new StandSkinsLoader();
		}
		ResourceLocation id = JojoMod.resLoc("standskins");
		event.addListener(id, instance);
		// We need to add the sound resources from skins to SoundManager#soundCache, otherwise these sounds won't play.
		// SoundManager#apply clears that map, so we need to apply the Stand skins after SoundManager does its logic.
		// In case of 1.21.1 backport: 
		//     the Stand-specific sounds will have to be in the main assets folder and have unique names; 
		//     adding new sounds via Stand skins will require a sounds.json inside the skin.
		event.addDependency(VanillaClientListeners.SOUNDS, id);
	}
	
	public static StandSkinsLoader getInstance() {
		return instance;
	}

	
	private final Map<ResourceLocation, Map<ResourceLocation, StandSkin>> skins = new HashMap<>();
	private final Map<ResourceLocation, List<StandSkin>> skinsByStand = new HashMap<>();
	private final Map<ResourceLocation, StandSkin> defaultSkins = new HashMap<>();
	
	public StandSkin getDefaultSkin(ResourceLocation standId) {
		return defaultSkins.get(standId);
	}
	
	public StandSkin getSkin(StandInstance standInstance) {
		Optional<ResourceLocation> selectedSkin = standInstance.getSelectedSkin();
		ResourceLocation standId = standInstance.getStandType().getId();
		return getSkinFromId(standId, selectedSkin);
	}
	
	public StandSkin getSkinFromId(ResourceLocation standId, Optional<ResourceLocation> selectedSkin) {
		if (standId == null) return null;
		
		if (selectedSkin.isPresent()) {
			Map<ResourceLocation, StandSkin> standSkins = skins.get(standId);
			if (standSkins != null) {
				StandSkin skin = standSkins.getOrDefault(selectedSkin.get(), null);
				if (skin != null) {
					return skin;
				}
			}
		}
		
		return getDefaultSkin(standId);
	}
	
	public ResourceLocation getDefaultSkinId(StandType standType) {
		return standType.getId();
	}
	
	public Stream<StandSkin> getStandSkins(ResourceLocation standId) {
		List<StandSkin> skins = skinsByStand.get(standId);
		return skins != null ? skins.stream() : Stream.empty();
	}

	
	public static ResourceLocation remap(ResourceLocation assetPath, ResourceLocation skinId) {
		return ResourceLocation.fromNamespaceAndPath(
				skinId.getNamespace(), 
				"stand_skins/" + 
				skinId.getPath() + "/" + 
				"assets/" + 
				assetPath.getNamespace() + "/" + assetPath.getPath());
	}
	
	@Override
	protected Map<ResourceLocation, StandSkinResourceBuilder> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
		Map<ResourceLocation, StandSkinResourceBuilder> skins = new HashMap<>();

		try (Zone zone = profiler.zone(JojoMod.MOD_ID + "_stand_skins")) {
			Map<ResourceLocation, List<Resource>> allResourcesMap = resourceManager.listResourceStacks("stand_skins", path -> true);
			for (var resourceEntry : allResourcesMap.entrySet()) {
				/*
				 *				  	filePath[0]	 filePath[1]...	   		[2]	 	[3]			 [4]								      	[5]
				   assets/my_skins/stand_skins/cool_star_platinum_skin/assets/jojo_ripples/... (/textures/geo/animations/lang/sounds/...)/.../
							  ^						  ^						^													    ^
					   skin namespace				skin path				  in-mod namespace for assets used by the stand		    the actual asset file is most likely here
						(arbitrary*)				(arbitrary*)				  (must match the namespace from stand id)			     (or it is a subdirectory, and the assets further down the path)
						
					   the resourceEntry.getKey() will look like this:
				   my_skins:stand_skins/cool_star_platinum_skin/assets/jojo_ripples/...
					  
					   (*) - for the skin to be considered the "default" one, the skin's 'namespace:path' pair must be the same as in the stand's id
				   
				   
					   for the main file (with stand_type, color, etc.) the path is:
				   assets/my_skins/stand_skins/cool_star_platinum_skin/skin.json
				 */
				ResourceLocation path = resourceEntry.getKey();
				String[] filePath = path.getPath().split("/");
				boolean isMainSkinFile = filePath.length == 3 && "skin.json".equals(filePath[2]);
				boolean isResource = filePath.length >= 5 && "assets".equals(filePath[2]);
				if (isMainSkinFile || isResource) {
					ResourceLocation skinId = ResourceLocation.fromNamespaceAndPath(path.getNamespace(), filePath[1]);
					StandSkinResourceBuilder skinBuilder = skins.computeIfAbsent(skinId, StandSkinResourceBuilder::new);
					if (isMainSkinFile) {
						for (var resource : resourceEntry.getValue()) {
							try (var reader = resource.openAsReader()) {
								JsonObject json = JSONUtil.parse(reader);
								loadSkinInfo(json, skinBuilder, JojoMod.getLogger());
							} catch (Exception e) {
								JojoMod.getLogger().error("Failed to read {} data for Stand skin {}", filePath[2], skinId, e);
							}
						}
					}
					else {
						String resNamespace = filePath[3];
						StringBuilder pathStr = new StringBuilder();
						for (int i = 5; i < filePath.length; i++) {
							if (i > 5) pathStr.append("/");
							pathStr.append(filePath[i]);
						}
						String resPathWithExt = pathStr.toString();
						String resType = filePath[4];
						loadResource(resourceEntry.getValue(), resType, resNamespace, resPathWithExt, skinBuilder, JojoMod.getLogger(), path);
					}
				}
			}
		}
		
		return skins;
	}
	
	
	// XXX extensible stand skins?
	public static class StandSkinResourceBuilder {
		private final ResourceLocation skinId;
		private ResourceLocation standId;
		private int uiColor = 0xffffff;
		private Map<ResourceLocation, LayerDefinition> models;
		private Map<ResourceLocation, AnimationSet.Builder> animations;
		private Map<ResourceLocation, WeighedSoundEvents> soundEvents;
		private Map<ResourceLocation, Pair<ResourceLocation, Resource>> soundFiles;
		
		private StandSkinResourceBuilder(ResourceLocation skinId) {
			this.skinId = skinId;
		}
		
		private boolean isValidSkin(Logger logger) {
			if (standId == null) {
				logger.error("Stand skin {} doesn't specify the Stand it belongs to! (Missing \"stand_type\")", skinId);
				return false;
			}
			return true;
		}
		
		private StandSkin makeSkin() {
			StandSkin skin = new StandSkin(skinId, standId);
			if (models != null) skin.withModels(models);
			if (animations != null) skin.withAnimations(animations);
			if (soundEvents != null) skin.withSoundEvents(soundEvents);
			if (soundFiles != null) skin.withSounds(soundFiles.entrySet().stream().collect(Collectors.toMap(
					Map.Entry::getKey, entry -> entry.getValue().getFirst())));
			return skin;
		}
	}
	
	
	private void loadSkinInfo(JsonObject skinInfoJson, StandSkinResourceBuilder builder, Logger logger) {
		ResourceLocation.CODEC.decode(JsonOps.INSTANCE, skinInfoJson.get("stand_type")).ifSuccess(res -> builder.standId = res.getFirst());
		if (skinInfoJson.has("color")) {
			builder.uiColor = JSONUtil.parseColor(skinInfoJson.get("color"));
		}
	}
	
	private static final String SOUND_EXTENSION = ".ogg";
	private void loadResource(List<Resource> resource, String resType, String resNamespace, String resPathWithExt, 
			StandSkinResourceBuilder builder, Logger logger, ResourceLocation fullFilePath) {
		switch (resType) {
			// XXX (stand skin) merge gecko and bb models (+ test the ParseModEntityModel.merge function)
			// It is possible to create two models: "geo" model with the regular cubes, and a "bb" one with just the meshes.
			// This is implemented to reduce overhead when we need a model with meshes, since models in the Generic Blockbench format generally take longer to load.
			case "geo" -> {
				readModel(resource, builder, resNamespace, resPathWithExt, Format.GECKO, ".geo.json");
			}
			case "bb" -> {
				readModel(resource, builder, resNamespace, resPathWithExt, Format.GENERIC, ".bbmodel");
			}
			case "animations" -> {
				var json = readLastResource(resource, null, JSONUtil::parse, builder.skinId, resNamespace, resPathWithExt, ".animation.json");
				if (json != null) {
					if (builder.animations == null) builder.animations = new HashMap<>();
					ResourceLocation path = json.getFirst();
					AnimationLoader.addAnimsToAnimSet(json.getSecond(), builder.animations.computeIfAbsent(path, __ -> new AnimationSet.Builder()), path);
				}
			}
			case "textures" -> {
				// XXX (stand skin) load sprites
			}
			case "lang" -> {
				// TODO (stand skin) load lang
			}
			case "sounds" -> {
				if (resPathWithExt.endsWith(SOUND_EXTENSION)) {
					if (builder.soundFiles == null) {
						builder.soundFiles = new HashMap<>();
					}
					ResourceLocation soundLocation = ResourceLocation.fromNamespaceAndPath(resNamespace, 
							StringUtil.substrBack(resPathWithExt, SOUND_EXTENSION.length()));
					Resource soundResource = resource.get(resource.size() - 1);
					builder.soundFiles.put(soundLocation, Pair.of(fullFilePath, soundResource));
				}
			}
			case "sounds.json" -> {
				loadSoundsJson(resource, builder, resNamespace);
			}
			default -> {}
		}
	}
	
	
	@Nullable
	private <T> Pair<ResourceLocation, T> readLastResource(List<Resource> resource, 
			@Nullable Function<Resource, T> add, @Nullable Function<BufferedReader, T> read, 
			ResourceLocation skinId, String resNamespace, String resPathWithExt, String... fileExtensions) {
		Pair<ResourceLocation, List<T>> resourceRead = readResources(
				Collections.singletonList(resource.get(resource.size() - 1)), 
				add, read, skinId, resNamespace, resPathWithExt, fileExtensions);
		return resourceRead != null ? resourceRead.mapSecond(list -> list.get(0)) : null;
	}
	
	@Nullable
	private <T> Pair<ResourceLocation, List<T>> readResources(List<Resource> resource, 
			@Nullable Function<Resource, T> addResource, @Nullable Function<BufferedReader, T> orReadResource, 
			ResourceLocation skinId, String resNamespace, String resPathWithExt, String... fileExtensions) {
		if (resource.isEmpty()) {
			throw new IllegalArgumentException();
		}
		if (fileExtensions.length == 0) {
			ResourceLocation path = ResourceLocation.fromNamespaceAndPath(resNamespace, resPathWithExt);
			return readResources(resource, addResource, orReadResource, skinId, path).map(res -> Pair.of(path, res)).orElse(null);
		}
		else {
			for (String ext : fileExtensions) {
				if (resPathWithExt.endsWith(ext)) {
					ResourceLocation path = ResourceLocation.fromNamespaceAndPath(resNamespace, StringUtil.substrBack(resPathWithExt, ext.length()));
					return readResources(resource, addResource, orReadResource, skinId, path).map(res -> Pair.of(path, res)).orElse(null);
				}
			}
		}
		return null;
	}
	
	private <T> Optional<List<T>> readResources(List<Resource> resource, 
			@Nullable Function<Resource, T> addResource, @Nullable Function<BufferedReader, T> orReadResource, 
			ResourceLocation skinId, ResourceLocation path) {
		List<T> jsons = new ArrayList<>();
		for (Resource file : resource) {
			if (addResource != null) {
				try {
					T element = addResource.apply(file);
					Objects.requireNonNull(element);
					jsons.add(element);
				} catch (Exception e) {
					JojoMod.getLogger().warn("Failed to add resource {} for Stand skin {}", path, skinId, e);
				}
			}
			else if (orReadResource != null) {
				try (var reader = file.openAsReader()) {
					T element = orReadResource.apply(reader);
					Objects.requireNonNull(element);
					jsons.add(element);
				} catch (Exception e) {
					JojoMod.getLogger().warn("Failed to read {} for Stand skin {}", path, skinId, e);
				}
			}
			
		}
		
		return jsons.isEmpty() ? Optional.empty() : Optional.of(jsons);
	}
	
	
	private void readModel(List<Resource> resource, StandSkinResourceBuilder builder, 
			String resNamespace, String resPathWithExt, Format modelFormat, String... fileExtensions) {
		var json = readLastResource(resource, null, JSONUtil::parse, builder.skinId, resNamespace, resPathWithExt, fileExtensions);
		if (json != null) {
			var modelDefinition = ParseModEntityModel.parse(json.getSecond(), modelFormat);
			if (builder.models == null) builder.models = new HashMap<>();
			builder.models.put(json.getFirst(), modelDefinition);
		}
	}
	
	
	private static final Gson LOAD_SOUND_EVENTS_GSON = new GsonBuilder()
			.registerTypeHierarchyAdapter(Component.class, new Component.SerializerAdapter(RegistryAccess.EMPTY))
			.registerTypeAdapter(SoundEventRegistration.class, new SoundEventRegistrationSerializer())
			.create();
	private static final TypeToken<Map<String, SoundEventRegistration>> SOUND_EVENT_REGISTRATION_TYPE = new TypeToken<Map<String, SoundEventRegistration>>() {};
	
	private void loadSoundsJson(List<Resource> soundsJsonsRes, StandSkinResourceBuilder standSkin, String namespace) {
		Map<String, SoundEventRegistration> soundsReg = new HashMap<>();
		var readSoundsJsons = readResources(soundsJsonsRes, 
				null, JSONUtil::parse, standSkin.skinId, 
				standSkin.skinId.withPath("sounds.json"));
		readSoundsJsons.ifPresent(jsons -> {
			for (var json : jsons) {
				soundsReg.putAll(LOAD_SOUND_EVENTS_GSON.fromJson(json, SOUND_EVENT_REGISTRATION_TYPE));
			}
		});
		if (soundsReg.isEmpty()) return;
		if (standSkin.soundEvents == null) {
			standSkin.soundEvents = new HashMap<>();
		}
		for (var soundEventReg : soundsReg.entrySet()) {
			String soundName = soundEventReg.getKey();
			ResourceLocation soundEventLocation = ResourceLocation.fromNamespaceAndPath(namespace, soundName);
			SoundEventRegistration registration = soundEventReg.getValue();
			WeighedSoundEvents soundEvent = standSkin.soundEvents.computeIfAbsent(
					soundEventLocation, loc -> new WeighedSoundEvents(loc, registration.getSubtitle()));
			if (!registration.isReplace()) {
				JojoMod.getLogger().warn("Stand skin sounds do not support \'\"replace\" = false\' option (sound event {} from Stand skin {})", soundName, standSkin.skinId);
			}
			for (final Sound sound : registration.getSounds()) {
				final ResourceLocation soundLocation = sound.getLocation();
				Weighted<Sound> weighted;
				switch (sound.getType()) {
					case FILE:
						weighted = sound;
						break;
					case SOUND_EVENT:
						JojoMod.getLogger().error("Stand skin sounds currently do not support \"event\" registration type (sound event {} from Stand skin {})", soundName, standSkin.skinId);
						continue;
					default:
						throw new IllegalStateException("Unknown SoundEventRegistration type: " + sound.getType());
				}

				soundEvent.addSound(weighted);
			}
		}
	}
	
	
	@Override
	protected void apply(Map<ResourceLocation, StandSkinResourceBuilder> skinsRead, ResourceManager resourceManager, ProfilerFiller profiler) {
		Minecraft mc = Minecraft.getInstance();
		SoundManager soundManager = mc.getSoundManager();
		Map<ResourceLocation, Resource> soundCache = ClientReflection.getSoundCache(soundManager);
		SoundEngine soundEngine = ClientReflection.getSoundEngine(soundManager);
		
		this.skins.clear();
		for (var skinBuilder : skinsRead.values()) {
			if (skinBuilder.isValidSkin(JojoMod.getLogger())) {
				// TODO move the makeSkin call to prepare?
				StandSkin skin = skinBuilder.makeSkin();
				this.skins.computeIfAbsent(skinBuilder.standId, __ -> new HashMap<>()).put(skinBuilder.skinId, skin);
				if (skin.skinId.equals(skinBuilder.standId)) {
					this.defaultSkins.put(skinBuilder.standId, skin);
				}
				
				if (skinBuilder.soundFiles != null) {
					skinBuilder.soundFiles.values().forEach(soundEntry -> {
						soundCache.put(soundEntry.getFirst(), soundEntry.getSecond());
					});
				}
				if (skinBuilder.soundEvents != null) {
					skinBuilder.soundEvents.values().forEach(soundEvent -> {
						soundEvent.preloadIfRequired(soundEngine);
					});
				}
			}
		}
		sortSkins();
		JojoMod.getLogger().info("Loaded {} Stand skins", skins.size());
	}
	
	private void sortSkins() {
		this.skinsByStand.clear();
		for (var standSkinsEntry : skins.entrySet()) {
			ResourceLocation standId = standSkinsEntry.getKey();
			List<StandSkin> standSkins = standSkinsEntry.getValue().values().stream()
					.sorted(Comparator.<StandSkin>
						comparingInt(skin -> getSkinPriority(skin, standId))
						.thenComparing(skin -> skin.skinId))
					.toList();
			skinsByStand.put(standId, Collections.unmodifiableList(standSkins));
		}
	}
	
	private static int getSkinPriority(StandSkin skin, ResourceLocation standId) {
		if (standId.equals(skin.skinId)) return 1;
		if (standId.getNamespace().equals(skin.skinId.getNamespace())) return 2;
		return 3;
	}
	
}
