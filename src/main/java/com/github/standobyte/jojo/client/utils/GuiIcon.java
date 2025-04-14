package com.github.standobyte.jojo.client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public record GuiIcon(ResourceLocation file, float offsetU, float offsetV, float widthU, float heightV, float texWidth, float texHeight) {

	public void render(GuiGraphics guiGraphics, float x, float y) {
		BlitFloat.blitFloat(guiGraphics, Minecraft.getInstance(), file, x, y, offsetU, offsetV, widthU, heightV, texWidth, texHeight);
	}
	
}
