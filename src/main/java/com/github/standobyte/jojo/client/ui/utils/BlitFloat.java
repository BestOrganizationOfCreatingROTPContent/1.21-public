package com.github.standobyte.jojo.client.ui.utils;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

/**
 * UI rendering stuff changed between versions, so these functions are still kinda error-prone.
 */
public class BlitFloat {
	public static final int NO_TINT = 0xFFFFFFFF;
	
//	public static void blitFloat(GuiGraphics guiGraphics, Minecraft mc, ResourceLocation texture, 
//			float pX, float pY, float pUOffset, float pVOffset, float pWidth, float pHeight) {
//		innerBlitFloat(guiGraphics, mc, RenderType::guiTextured, texture, 
//				pX, pX + pWidth, pY, pY + pHeight, 
//				pWidth, pHeight, pUOffset, pVOffset, pWidth, pHeight, 
//				-1);
//	}
//	
//	public static void blitFloat(GuiGraphics guiGraphics, Minecraft mc, ResourceLocation texture, 
//			float pX, float pY, float pUOffset, float pVOffset, 
//			float pUWidth, float pVHeight, float pTextureWidth, float pTextureHeight) {
//		innerBlitFloat(guiGraphics, mc, RenderType::guiTextured, texture, 
//				pX, pX + pUWidth, pY, pY + pVHeight, 
//				pUWidth, pVHeight, pUOffset, pVOffset, pTextureWidth, pTextureHeight, 
//				-1);
//	}
//	
//	public static void blitFloat(GuiGraphics guiGraphics, Minecraft mc, ResourceLocation texture, 
//			float pX, float pY, float pWidth, float pHeight, 
//			float pUOffset, float pVOffset, float pUWidth, float pVHeight, float pTextureWidth, float pTextureHeight) {
//		innerBlitFloat(guiGraphics, mc, RenderType::guiTextured, texture, 
//				pX, pX + pWidth, pY, pY + pHeight, 
//				pUWidth, pVHeight, pUOffset, pVOffset, pTextureWidth, pTextureHeight, 
//				-1);
//	}
//	
//	public static void innerBlitFloat(GuiGraphics guiGraphics, Minecraft mc, 
//			Function<ResourceLocation, RenderType> renderTypeGetter, ResourceLocation texture, 
//			float pX1, float pX2, float pY1, float pY2, 
//			float pUWidth, float pVHeight, float pUOffset, float pVOffset, float pTextureWidth, float pTextureHeight, 
//			int color) {
//		innerBlitFloat(guiGraphics, mc, renderTypeGetter.apply(texture), 
//				pX1, pX2, pY1, pY2, 
//				(pUOffset + 0.0F) / pTextureWidth, 
//				(pUOffset + pUWidth) / pTextureWidth, 
//				(pVOffset + 0.0F) / pTextureHeight, 
//				(pVOffset + pVHeight) / pTextureHeight, 
//				color);
//	}
//	
//	public static void innerBlitFloat(GuiGraphics guiGraphics, Minecraft mc, RenderType renderType, 
//			float x1, float x2, float y1, float y2, 
//			float minU, float maxU, float minV, float maxV, 
//			int color) {
//		Matrix4f matrix4f = guiGraphics.pose().last().pose();
//		MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
//		VertexConsumer vertexconsumer = bufferSource.getBuffer(renderType);
//		vertexconsumer.addVertex(matrix4f, x1, y1, 0.0F).setUv(minU, minV).setColor(color);
//		vertexconsumer.addVertex(matrix4f, x1, y2, 0.0F).setUv(minU, maxV).setColor(color);
//		vertexconsumer.addVertex(matrix4f, x2, y2, 0.0F).setUv(maxU, maxV).setColor(color);
//		vertexconsumer.addVertex(matrix4f, x2, y1, 0.0F).setUv(maxU, minV).setColor(color);
//	}
	


	public static void blit(PoseStack poseStack, Minecraft mc, TextureAtlasSprite sprite,
			float x0, float y0, float xWidth, float yHeight, float blitOffset,
			int tint) {
		float u0 = sprite.getU0();
		float v0 = sprite.getV0();
		float uWidth = sprite.getU1() - u0;
		float vHeight = sprite.getV1() - v0;
		blit(poseStack, mc, 
				sprite.atlasLocation(),
				x0, y0, xWidth, yHeight, blitOffset,
				u0, v0, uWidth, vHeight, 1, 1, tint);
	}

	public static void blit(PoseStack poseStack, Minecraft mc, ResourceLocation sprite,
			float x0, float y0, float xWidth, float yHeight, float blitOffset,
			int tint) {
		blit(poseStack, mc, 
				sprite,
				x0, y0, xWidth, yHeight, blitOffset,
				0, 0, xWidth, yHeight, 1, 1, tint);
	}

	public static void blit(PoseStack poseStack, Minecraft mc, ResourceLocation texture,
			float x0, float y0, float xWidth, float yHeight, float blitOffset,
			float u0, float v0, float uWidth, float vHeight, float textureWidth, float textureHeight, 
			int tint) {
		RenderSystem.setShaderTexture(0, texture);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		Matrix4f matrix4f = poseStack.last().pose();
		BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		float x1 = x0 + xWidth;
		float y1 = y0 + yHeight;
		float u1 = u0 + uWidth;
		float v1 = v0 + vHeight;
		u0 /= textureWidth;
		u1 /= textureWidth;
		v0 /= textureHeight;
		v1 /= textureHeight;
		bufferbuilder.addVertex(matrix4f, x0, y0, blitOffset).setUv(u0, v0).setColor(tint);
		bufferbuilder.addVertex(matrix4f, x0, y1, blitOffset).setUv(u0, v1).setColor(tint);
		bufferbuilder.addVertex(matrix4f, x1, y1, blitOffset).setUv(u1, v1).setColor(tint);
		bufferbuilder.addVertex(matrix4f, x1, y0, blitOffset).setUv(u1, v0).setColor(tint);
		BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
	}
}
