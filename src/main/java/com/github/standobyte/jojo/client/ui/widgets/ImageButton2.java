package com.github.standobyte.jojo.client.ui.widgets;

import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ImageButton2 extends Button {
	public GuiIcon spriteEnabled;
	public GuiIcon spriteDisabled;
	public GuiIcon spriteEnabledFocused;
	public GuiIcon spriteDisabledFocused;

	public ImageButton2(int x, int y, int width, int height, 
			GuiIcon enabled, GuiIcon disabled, GuiIcon enabledFocused, GuiIcon disabledFocused, 
			Button.OnPress onPress) {
		this(x, y, width, height, 
				enabled, disabled, enabledFocused, disabledFocused, 
				onPress, CommonComponents.EMPTY);
	}

	public ImageButton2(int x, int y, int width, int height, 
			GuiIcon enabled, GuiIcon disabled, GuiIcon enabledFocused, GuiIcon disabledFocused, 
			Button.OnPress onPress, Component message) {
		super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
		this.spriteEnabled = enabled;
		this.spriteDisabled = disabled;
		this.spriteEnabledFocused = enabledFocused;
		this.spriteDisabledFocused = disabledFocused;
	}

	public ImageButton2(int width, int height, 
			GuiIcon enabled, GuiIcon disabled, GuiIcon enabledFocused, GuiIcon disabledFocused, 
			Button.OnPress onPress, Component message) {
		this(0, 0, width, height, 
				enabled, disabled, enabledFocused, disabledFocused, 
				onPress, message);
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		GuiIcon icon;
		if (this.isActive()) {
			icon = this.isHoveredOrFocused() ? this.spriteEnabledFocused : this.spriteEnabled;
		} else {
			icon = this.isHoveredOrFocused() ? this.spriteDisabledFocused : this.spriteDisabled;
		}
		if (icon != null) {
			icon.render(guiGraphics.pose(), this.getX(), this.getY(), this.width, this.height, BlitFloat.NO_TINT);
		}
	}

}
