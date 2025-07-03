package com.github.standobyte.jojo.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.entityrender.parsemodel.gecko.GeckoModelFormat;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.util.JSONUtil;
import com.github.standobyte.jojo.util.StringUtil;
import com.google.gson.JsonElement;

import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.Zone;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;

public class RotpGeckoModelLoader extends SimplePreparableReloadListener<Map<ResourceLocation, LayerDefinition>> {
	private static RotpGeckoModelLoader instance;
	
	@ApiStatus.Internal
	public static void init(AddClientReloadListenersEvent event) {
		if (instance == null) {
			instance = new RotpGeckoModelLoader();
		}
		event.addListener(JojoMod.resLoc("ripples_models"), instance);
	}
	
	public static RotpGeckoModelLoader getInstance() {
		return instance;
	}
	
	
	private Map<ResourceLocation, LayerDefinition> models = new HashMap<>();
	
	public LayerDefinition getModelDefinition(ResourceLocation path) {
		return models.get(path);
	}
	

	private static final String DIR = "geo/rotp";
	private static final String EXTENSION = ".geo.json";
	
	@Override
	protected Map<ResourceLocation, LayerDefinition> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
		Map<ResourceLocation, LayerDefinition> models = new HashMap<>();

		try (Zone zone = profiler.zone(JojoMod.MOD_ID)) {
			Map<ResourceLocation, Resource> resources = resourceManager.listResources(DIR, path -> path.getPath().endsWith(EXTENSION));
			for (var resourceEntry : resources.entrySet()) {
				ResourceLocation resourcePathFull = resourceEntry.getKey();
				ResourceLocation modelPath = resourcePathFull.withPath(
						StringUtil.trimEnding(resourceEntry.getKey().getPath(), EXTENSION).substring(DIR.length() + 1));
				JsonElement json = null;
				try (var reader = resourceEntry.getValue().openAsReader()) {
					json = JSONUtil.parse(reader);
				}
				catch (IOException e) {
					JojoMod.getLogger().error("Failed to parse model {}", modelPath, e);
				}
				if (json != null) {
					LayerDefinition model = GeckoModelFormat.parseGeckoModel(json);
					models.put(modelPath, model);
				}
			}
		}
		
		return models;
	}
	
	@Override
	protected void apply(Map<ResourceLocation, LayerDefinition> skinsRead, ResourceManager resourceManager, ProfilerFiller profiler) {
		this.models.clear();
		this.models.putAll(skinsRead);
		JojoMod.getLogger().info("Loaded {} models", this.models.size());
	}

}
