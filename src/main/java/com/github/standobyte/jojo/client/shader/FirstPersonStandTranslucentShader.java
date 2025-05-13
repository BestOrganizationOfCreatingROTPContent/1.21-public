package com.github.standobyte.jojo.client.shader;

import java.util.function.BiFunction;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.util.reflection.ClientReflection;
import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.TriState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ConfigureMainRenderTargetEvent;
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

    public static final BiFunction<ResourceLocation, Boolean, RenderType> ENTITY_TRANSLUCENT_RENDER_TYPE = Util.memoize(
        (texture, outline) -> {
            RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(RenderType.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.FALSE, false))
                .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                .setCullState(RenderType.NO_CULL)
                .setLightmapState(RenderType.LIGHTMAP)
                .setOverlayState(RenderType.OVERLAY)
                .setOutputState(STAND_TRANSLUCENT_TARGET)
                .createCompositeState(outline);
            return RenderType.create("jojo_ripples:stand_translucent", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, true, true, state);
        }
    );
	
	@SubscribeEvent
	public static void mcInit(ConfigureMainRenderTargetEvent event) {
		Minecraft mc = Minecraft.getInstance();
	    standTranslucencyFrameBuffer = new MainTarget(mc.getWindow().getWidth(), mc.getWindow().getHeight(), false);
	    standTranslucencyFrameBuffer.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
	    standTranslucencyFrameBuffer.clear();
	}
	
	@SubscribeEvent
	public static void mcInit2(RenderLevelStageEvent.RegisterStageEvent event) {
		resourcePoolCache = ClientReflection.getResourcePool(Minecraft.getInstance().gameRenderer);
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void createRenderBuffer(RegisterRenderBuffersEvent event) {
		// XXX limit the render types?
		standTranslucencyBufferSource = MultiBufferSource.immediateWithBuffers(new Object2ObjectLinkedOpenHashMap<>(), new ByteBufferBuilder(786432));
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
	

	private static GraphicsResourceAllocator resourcePoolCache;
	public static void setupStandFirstPersonBuffer() {
		Minecraft mc = Minecraft.getInstance();
		standTranslucencyFrameBuffer.clear();
		standTranslucencyFrameBuffer.copyDepthFrom(mc.getMainRenderTarget());
	}

	@SuppressWarnings("deprecation")
	public static void applyEffect() {
		Minecraft mc = Minecraft.getInstance();
		ResourceLocation postEffectId = JojoMod.resLoc("fp_stand_translucent");

		RenderSystem.disableBlend();
		RenderSystem.disableDepthTest();
		RenderSystem.resetTextureMatrix();
		PostChain postchain = mc.getShaderManager().getPostChain(postEffectId, LevelTargetBundle.MAIN_TARGETS);
		if (postchain != null) {
			postchain.process(standTranslucencyFrameBuffer, resourcePoolCache);
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
		standTranslucencyFrameBuffer.blitAndBlendToScreen(mc.getWindow().getWidth(), mc.getWindow().getHeight());
		RenderSystem.disableBlend();
		RenderSystem.defaultBlendFunc();
	}
	
	
	public static void resize(int width, int height) {
		if (standTranslucencyFrameBuffer != null) {
			standTranslucencyFrameBuffer.resize(width, height);
		}
	}

}
