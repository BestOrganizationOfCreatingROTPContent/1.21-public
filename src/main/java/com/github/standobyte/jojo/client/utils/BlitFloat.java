package com.github.standobyte.jojo.client.utils;

import java.util.function.Function;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * UI rendering stuff changed between versions, so these functions are still kinda error-prone.
 */
public class BlitFloat {
	
	public static void blitFloat(GuiGraphics guiGraphics, Minecraft mc, ResourceLocation texture, 
			float pX, float pY, float pUOffset, float pVOffset, float pWidth, float pHeight) {
		innerBlitFloat(guiGraphics, mc, RenderType::guiTextured, texture, 
				pX, pX + pWidth, pY, pY + pHeight, 
				pWidth, pHeight, pUOffset, pVOffset, pWidth, pHeight, 
				-1);
	}
	
	public static void blitFloat(GuiGraphics guiGraphics, Minecraft mc, ResourceLocation texture, 
			float pX, float pY, float pUOffset, float pVOffset, 
			float pUWidth, float pVHeight, float pTextureWidth, float pTextureHeight) {
		innerBlitFloat(guiGraphics, mc, RenderType::guiTextured, texture, 
				pX, pX + pUWidth, pY, pY + pVHeight, 
				pUWidth, pVHeight, pUOffset, pVOffset, pTextureWidth, pTextureHeight, 
				-1);
	}
	
//	public static void blitFloat(GuiGraphics guiGraphics, Minecraft mc, ResourceLocation texture, 
//			float pX, float pY, float pWidth, float pHeight, TextureAtlasSprite pSprite) {
//		innerBlitFloat(guiGraphics, mc, RenderType::guiTextured, texture, 
//				pX, pX + pWidth, pY, pY + pHeight, 
//				pSprite.getU0(), pSprite.getU1(), pSprite.getV0(), pSprite.getV1(), 
//				-1);
//	}
	
	public static void blitFloat(GuiGraphics guiGraphics, Minecraft mc, ResourceLocation texture, 
			float pX, float pY, float pWidth, float pHeight, 
			float pUOffset, float pVOffset, float pUWidth, float pVHeight, float pTextureWidth, float pTextureHeight) {
		innerBlitFloat(guiGraphics, mc, RenderType::guiTextured, texture, 
				pX, pX + pWidth, pY, pY + pHeight, 
				pUWidth, pVHeight, pUOffset, pVOffset, pTextureWidth, pTextureHeight, 
				-1);
	}
	
//	public static void blitFloat(GuiGraphics guiGraphics, Minecraft mc, Function<ResourceLocation, RenderType> renderTypeGetter, ResourceLocation texture, 
//			float pX, float pY, float pWidth, float pHeight, TextureAtlasSprite pSprite,
//			float uOffsetMult, float uWidthMult, float vOffsetMult, float vHeightMult) {
//		float u0 = pSprite.getU0();
//		float u1 = pSprite.getU1();
//		float v0 = pSprite.getV0();
//		float v1 = pSprite.getV1();
//		float width = u1 - u0;
//		float height = v1 - v0;
//		u0 += width * uOffsetMult;
//		v0 += height * vOffsetMult;
//		u1 = u0 + width * uWidthMult;
//		v1 = v0 + height * vHeightMult;
//		innerBlitFloat(guiGraphics, mc, renderTypeGetter, texture, 
//				pX, pX + pWidth, pY, pY + pHeight, 
//				u0, u1, v0, v1, 
//				-1);
//	}
	
	public static void innerBlitFloat(GuiGraphics guiGraphics, Minecraft mc, Function<ResourceLocation, RenderType> renderTypeGetter, ResourceLocation texture, 
			float pX1, float pX2, float pY1, float pY2, 
			float pUWidth, float pVHeight, float pUOffset, float pVOffset, float pTextureWidth, float pTextureHeight, 
			int color) {
		innerBlitFloat(guiGraphics, mc, renderTypeGetter, texture, 
				pX1, pX2, pY1, pY2, 
				(pUOffset + 0.0F) / pTextureWidth, 
				(pUOffset + pUWidth) / pTextureWidth, 
				(pVOffset + 0.0F) / pTextureHeight, 
				(pVOffset + pVHeight) / pTextureHeight, 
				-1);
	}
	
	public static void innerBlitFloat(GuiGraphics guiGraphics, Minecraft mc, Function<ResourceLocation, RenderType> renderTypeGetter, ResourceLocation texture, 
			float x1, float x2, float y1, float y2, 
			float minU, float maxU, float minV, float maxV, 
			int color) {
		RenderType renderType = renderTypeGetter.apply(texture);
		Matrix4f matrix4f = guiGraphics.pose().last().pose();
		MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
		VertexConsumer vertexconsumer = bufferSource.getBuffer(renderType);
		vertexconsumer.addVertex(matrix4f, x1, y1, 0.0F).setUv(minU, minV).setColor(color);
		vertexconsumer.addVertex(matrix4f, x1, y2, 0.0F).setUv(minU, maxV).setColor(color);
		vertexconsumer.addVertex(matrix4f, x2, y2, 0.0F).setUv(maxU, maxV).setColor(color);
		vertexconsumer.addVertex(matrix4f, x2, y1, 0.0F).setUv(maxU, minV).setColor(color);
	}
}
