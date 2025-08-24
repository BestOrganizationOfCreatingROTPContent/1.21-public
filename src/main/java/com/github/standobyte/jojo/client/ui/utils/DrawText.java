package com.github.standobyte.jojo.client.ui.utils;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class DrawText {

	public static void drawRightAlignedString(GuiGraphics gui, Font font, String text, 
			int x, int y, int color, boolean shadow) {
		gui.drawString(font, text, x - font.width(text), y, color, shadow);
	}

	public static void drawRightAlignedString(GuiGraphics gui, Font font, Component text, 
			int x, int y, int color, boolean shadow) {
		gui.drawString(font, text, x - font.width(text), y, color, shadow);
	}
}
