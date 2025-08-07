package com.github.standobyte.jojo.client.shader;

import com.github.standobyte.jojo.client.rendertype.CustomMultiBufferSource;
import com.github.standobyte.jojo.core.JojoMod;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterRenderBuffersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;

@EventBusSubscriber(modid = JojoMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class FirstPersonStandTranslucentShader {
    public static RenderTarget standTranslucencyFrameBuffer;
    public static MultiBufferSource.BufferSource standTranslucencyBufferSource;
    public static boolean usedThisFrame = false;

	public static final RenderStateShard.OutputStateShard STAND_TRANSLUCENT_TARGET = new RenderStateShard.OutputStateShard(
			"stand_translucent", () -> standTranslucencyFrameBuffer.bindWrite(false), () -> {});

//	@SubscribeEvent
	public static void bufferInit(/*ConfigureMainRenderTargetEvent event*/) {
		Minecraft mc = Minecraft.getInstance();
	    standTranslucencyFrameBuffer = new MainTarget(mc.getWindow().getWidth(), mc.getWindow().getHeight()/*, false*/);
	    standTranslucencyFrameBuffer.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
	    standTranslucencyFrameBuffer.clear(Minecraft.ON_OSX);
	}
	
//	@SubscribeEvent
//	public static void mcInit2(RenderLevelStageEvent.RegisterStageEvent event) {
//		resourcePoolCache = ClientReflection.getResourcePool(Minecraft.getInstance().gameRenderer);
//	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void createRenderBuffer(RegisterRenderBuffersEvent event) {
		standTranslucencyBufferSource = new CustomMultiBufferSource(new ByteBufferBuilder(786432), new Object2ObjectLinkedOpenHashMap<>(), 
				STAND_TRANSLUCENT_TARGET);
	}

	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
		NeoForge.EVENT_BUS.addListener(FirstPersonStandTranslucentShader::frameRenderCallback);
	}
	
	public static void frameRenderCallback(RenderLevelStageEvent event) {
		RenderLevelStageEvent.Stage stage = event.getStage();
		if (stage == RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) {
			usedThisFrame = false;
		}
		if (usedThisFrame) {
			if (stage == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
				setupStandFirstPersonBuffer();
		    	standTranslucencyBufferSource.endBatch();
				applyEffect();
				Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
			}
			if (stage == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
				blitBuffer();
			}
		}
	}
	

//	private static GraphicsResourceAllocator resourcePoolCache;
	public static void setupStandFirstPersonBuffer() {
		Minecraft mc = Minecraft.getInstance();
		standTranslucencyFrameBuffer.clear(Minecraft.ON_OSX);
		standTranslucencyFrameBuffer.copyDepthFrom(mc.getMainRenderTarget());
	}

	private static final ResourceLocation POST_EFFECT_ID = JojoMod.resLoc("fp_stand_translucent");
//	@SuppressWarnings("deprecation")
	public static void applyEffect() {
		Minecraft mc = Minecraft.getInstance();

		RenderSystem.disableBlend();
		RenderSystem.disableDepthTest();
		RenderSystem.resetTextureMatrix();
//		PostChain postchain = mc.getShaderManager().getPostChain(POST_EFFECT_ID, LevelTargetBundle.MAIN_TARGETS);
		PostChain postchain = PostEffectCache.instance.getEffect(POST_EFFECT_ID, standTranslucencyFrameBuffer, true);
		if (postchain != null) {
			postchain.process(mc.getTimer().getGameTimeDeltaTicks());
//			postchain.process(standTranslucencyFrameBuffer, resourcePoolCache);
		}
	}
	
	public static void blitBuffer() {
		Minecraft mc = Minecraft.getInstance();
		
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(
				GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
				GlStateManager.SourceFactor.ZERO,
				GlStateManager.DestFactor.ONE
				);
//		standTranslucencyFrameBuffer.blitAndBlendToScreen(mc.getWindow().getWidth(), mc.getWindow().getHeight());
		standTranslucencyFrameBuffer.blitToScreen(mc.getWindow().getWidth(), mc.getWindow().getHeight(), false);
		RenderSystem.disableBlend();
		RenderSystem.defaultBlendFunc();
	}
	
	
	public static void resize(int width, int height) {
		if (standTranslucencyFrameBuffer != null) {
			standTranslucencyFrameBuffer.resize(width, height, Minecraft.ON_OSX);
		}
	}

}
