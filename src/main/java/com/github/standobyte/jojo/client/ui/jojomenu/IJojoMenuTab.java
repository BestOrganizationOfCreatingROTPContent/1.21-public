package com.github.standobyte.jojo.client.ui.jojomenu;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public interface IJojoMenuTab {
	void renderIcon(GuiGraphics guiGraphics, int x, int y);
	Component getName();
	boolean onClick(Minecraft mc, @Nullable Screen curScreen);
}
