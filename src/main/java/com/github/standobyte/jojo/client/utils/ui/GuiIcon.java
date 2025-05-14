package com.github.standobyte.jojo.client.utils.ui;

import org.joml.Matrix4f;

import com.github.standobyte.jojo.client.utils.BlitFloat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class GuiIcon {
	private final ResourceLocation file;
	private final float width;
	private final float height;
	private final float minU;
	private final float maxU;
	private final float minV;
	private final float maxV;

	public GuiIcon(ResourceLocation file, 
			float offsetU, float offsetV, 
			float widthU, float heightV, 
			float texWidth, float texHeight) {
		this.file = file;
		this.width = widthU;
		this.height = heightV;
		this.minU = offsetU / texWidth;
		this.maxU = (offsetU + widthU) / texWidth;
		this.minV = offsetV / texHeight;
		this.maxV = (offsetV + heightV) / texHeight;
	}

	public void render(GuiGraphics guiGraphics, float x, float y) {
		render(guiGraphics, x, y, 0xFFFFFFFF);
	}

	public void render(GuiGraphics guiGraphics, float x, float y, int color) {
		BlitFloat.innerBlitFloat(guiGraphics, Minecraft.getInstance(), RenderType.guiTextured(file), 
				x, x + width, y, y + height, 
				minU, maxU, minV, maxV, color);
	}

	public void render(PoseStack poseStack, float x, float y, int redByte, int greenByte, int blueByte, int alphaByte) {
        RenderType rendertype = RenderType.guiTextured(file);
        Matrix4f matrix4f = poseStack.last().pose();
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer vertexconsumer = bufferSource.getBuffer(rendertype);
        
        float x2 = x + width;
        float y2 = y + height;
        
        vertexconsumer.addVertex(matrix4f, x , y , 0.0F).setUv(minU, minV).setColor(redByte, greenByte, blueByte, alphaByte);
        vertexconsumer.addVertex(matrix4f, x , y2, 0.0F).setUv(minU, maxV).setColor(redByte, greenByte, blueByte, alphaByte);
        vertexconsumer.addVertex(matrix4f, x2, y2, 0.0F).setUv(maxU, maxV).setColor(redByte, greenByte, blueByte, alphaByte);
        vertexconsumer.addVertex(matrix4f, x2, y , 0.0F).setUv(maxU, minV).setColor(redByte, greenByte, blueByte, alphaByte);
	}
	
}
