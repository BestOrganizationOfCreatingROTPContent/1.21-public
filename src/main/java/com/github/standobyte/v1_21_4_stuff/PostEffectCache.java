package com.github.standobyte.v1_21_4_stuff;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.github.standobyte.jojo.client.ModClientResources;
import com.github.standobyte.jojo.core.JojoMod;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class PostEffectCache implements PreparableReloadListener, AutoCloseable {
	public static PostEffectCache instance;
	
	@SubscribeEvent
	public static void register(RegisterClientReloadListenersEvent event) {
		if (instance == null) {
			instance = new PostEffectCache();
			ModClientResources.closeables.add(instance);
		}
		event.registerReloadListener(instance);
	}
	
	protected Map<ResourceLocation, PostChain> cache = new HashMap<>();
	protected ResourceLocation curLoadedPath;
	protected PostChain curLoaded;
	
	public PostChain getEffect(ResourceLocation path, boolean keepLoaded) {
		return getEffect(path, Minecraft.getInstance().getMainRenderTarget(), keepLoaded);
	}
	
	public PostChain getEffect(ResourceLocation path, RenderTarget screenTarget, boolean keepLoaded) {
		ResourceLocation actualPath = path.withPath(p -> "shaders/post/" + p + ".json");
		if (path.equals(curLoadedPath)) {
			return curLoaded;
		}
		if (cache.containsKey(path)) {
			return cache.get(path);
		}
		
		Minecraft mc = Minecraft.getInstance();
		PostChain effect;
		try {
			effect = new PostChain(mc.getTextureManager(), 
					mc.getResourceManager(), screenTarget, actualPath);
			effect.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
		} catch (IOException e) {
			JojoMod.getLogger().error("Failed to load shader: {}", actualPath, e);
			effect = null;
			keepLoaded = true;
		} catch (JsonSyntaxException e) {
			JojoMod.getLogger().error("Failed to parse shader: {}", actualPath, e);
			effect = null;
			keepLoaded = true;
		}
		if (keepLoaded) {
			cache.put(path, effect);
		}
		
		if (this.curLoaded != null && this.curLoadedPath != null) {
			if (!cache.containsKey(curLoadedPath)) { // else - we kept it loaded intentionally, so we don't need to close it now
				this.curLoaded.close();
			}
		}
		this.curLoaded = effect;
		this.curLoadedPath = path;
		return effect;
	}
	
	public static void resize(int width, int height) {
		if (instance != null) {
			if (instance.curLoaded != null) {
				instance.curLoaded.resize(width, height);
			}
			for (PostChain effect : instance.cache.values()) {
				if (effect != null && effect != instance.curLoaded) effect.resize(width, height);
			}
		}
	}
	
	@Override
	public void close() {
		closeEffects();
	}
	
	protected void closeEffects() {
		if (curLoaded != null) {
			curLoaded.close();
		}
		for (PostChain effect : cache.values()) {
			// FIXME (!) reload - Rendersystem called from wrong thread
			if (effect != null && effect != curLoaded) effect.close();
		}
		curLoaded = null;
		curLoadedPath = null;
		cache.clear();
	}

	@Override
	public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager manager,
	        ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler,
			Executor backgroundExecutor, Executor gameExecutor) {
		return barrier.wait(null).thenRunAsync(this::closeEffects);
	}
}
