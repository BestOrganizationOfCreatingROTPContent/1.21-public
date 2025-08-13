package com.github.standobyte.jojo.client.ui.utils;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class DrawText {

	public static void drawRightAlignedString(GuiGraphics gui, Font font, String text, int x, int y, int color) {
		gui.drawString(font, text, x - font.width(text), y, color);
	}
}
