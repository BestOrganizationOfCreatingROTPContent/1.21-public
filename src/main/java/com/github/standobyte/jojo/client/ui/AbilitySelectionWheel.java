package com.github.standobyte.jojo.client.ui;

import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class AbilitySelectionWheel extends Screen implements ScreenLetsUseWASD {
	protected static final ResourceLocation DEFAULT_TEXTURE = JojoMod.resLoc("textures/ability_wheel.png");

	public AbilitySelectionWheel() {
		super(Component.translatable("jojo.screen.ability_selection_wheel"));
	}
	
	@Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    	super.render(guiGraphics, mouseX, mouseY, partialTick);

    	float width = 256;
    	float height = 256;
		float x = (this.width - width) / 2f;
		float y = (this.height - height) / 2f;
		ResourceLocation texture = DEFAULT_TEXTURE;
		BlitFloat.blit(guiGraphics.pose(), Minecraft.getInstance(), texture, 
				x, y, width, height, 0, 
				0, 0, width, height, 256, 256, 
				BlitFloat.NO_TINT);
    }

	@Override
    public boolean isPauseScreen() {
    	return false;
    }

	@Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}

}
