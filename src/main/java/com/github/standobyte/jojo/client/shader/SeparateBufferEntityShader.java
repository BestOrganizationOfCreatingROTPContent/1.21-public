package com.github.standobyte.jojo.client.shader;

import com.github.standobyte.jojo.client.rendertype.CustomMultiBufferSource;
import com.github.standobyte.v1_21_4_stuff.PostEffectCache;
import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public class SeparateBufferEntityShader {
	protected RenderTarget frameBuffer;
	protected MultiBufferSource.BufferSource bufferSource;
	protected boolean usedThisFrame = false;
	protected RenderStateShard.OutputStateShard targetShard;
	protected ResourceLocation postShaderId;
	
	public SeparateBufferEntityShader(Minecraft mc, String outputShardName, ResourceLocation postShaderId) {
		frameBuffer = new MainTarget(mc.getWindow().getWidth(), mc.getWindow().getHeight()/*, false*/);
		frameBuffer.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
		frameBuffer.clear(Minecraft.ON_OSX);
		targetShard = new RenderStateShard.OutputStateShard(outputShardName, () -> frameBuffer.bindWrite(false), () -> {});
		this.postShaderId = postShaderId;
	}
	
	protected void createBufferSource() {
		bufferSource = new CustomMultiBufferSource(
				new ByteBufferBuilder(786432), 
				new Object2ObjectLinkedOpenHashMap<>(), 
				targetShard);
	}
	
	
	public MultiBufferSource useBufferSourceThisFrame() {
		usedThisFrame = true;
		return bufferSource;
	}
	
	protected void frameRenderCallback(RenderLevelStageEvent event) {
		RenderLevelStageEvent.Stage stage = event.getStage();
		if (stage == RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) {
			this.usedThisFrame = false;
		}
		if (this.usedThisFrame) {
			if (stage == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
				this.setupBuffer();
				this.bufferSource.endBatch();
				this.applyEffect();
				Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
			}
			if (stage == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
				this.blitBuffer();
			}
		}
	}
	
	protected void setupBuffer() {
		Minecraft mc = Minecraft.getInstance();
		frameBuffer.clear(Minecraft.ON_OSX);
		frameBuffer.copyDepthFrom(mc.getMainRenderTarget());
	}

//	@SuppressWarnings("deprecation")
	protected void applyEffect() {
		Minecraft mc = Minecraft.getInstance();

		RenderSystem.disableBlend();
		RenderSystem.disableDepthTest();
		RenderSystem.resetTextureMatrix();
//		PostChain postchain = mc.getShaderManager().getPostChain(postShaderId, LevelTargetBundle.MAIN_TARGETS);
		PostChain postchain = PostEffectCache.instance.getEffect(postShaderId, frameBuffer, true);
		if (postchain != null) {
			postchain.process(mc.getTimer().getGameTimeDeltaTicks());
//			postchain.process(frameBuffer, EntityShaders.resourcePoolCache);
		}
	}
	
	protected void blitBuffer() {
		Minecraft mc = Minecraft.getInstance();
		
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(
				GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
				GlStateManager.SourceFactor.ZERO,
				GlStateManager.DestFactor.ONE
				);
//		frameBuffer.blitAndBlendToScreen(mc.getWindow().getWidth(), mc.getWindow().getHeight());
		frameBuffer.blitToScreen(mc.getWindow().getWidth(), mc.getWindow().getHeight(), false);
		RenderSystem.disableBlend();
		RenderSystem.defaultBlendFunc();
	}
	
	
	protected void resize(int width, int height) {
		if (frameBuffer != null) {
			frameBuffer.resize(width, height, Minecraft.ON_OSX);
		}
	}
}
