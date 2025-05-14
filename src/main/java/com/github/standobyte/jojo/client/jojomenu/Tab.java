package com.github.standobyte.jojo.client.jojomenu;

import java.util.Objects;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.utils.ui.GuiIcon;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.PowerType;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class Tab implements IJojoMenuTab {
	protected final TabCategory category;
	protected boolean isDisabled = false;
	protected final @Nullable PowerClass<?> powerClass;
	protected final @Nullable Supplier<? extends PowerType> powerType;
	
	public Tab(TabCategory category) {
		this(category, null, null);
	}
	
	protected Tab(TabCategory category, @Nullable PowerClass<?> powerClass, @Nullable Supplier<? extends PowerType> powerType) {
		this.category = category;
		category.tabs.add(this);
		this.powerClass = powerClass;
		this.powerType = powerType;
	}
	
	public Tab disable() {
		this.isDisabled = true;
		return this;
	}
	
	public boolean isActive() {
		if (isDisabled) return false;
		if (powerClass != null) {
			Player player = Minecraft.getInstance().player;
			if (player == null) return false;
			Power<?> power = powerClass.get(player);
			return power != null && power.hasPower() && (powerType == null || power.getPowerType() == powerType.get());
		}
		return true;
	}
	
	protected final TabCategory getCategory() {
		return category;
	}
	
	
	protected Supplier<? extends Screen> newScreen = () -> new PlaceholderScreen(Component.empty(), this.getCategory(), this);
	
	public Tab withScreen(Supplier<? extends Screen> newScreen) {
		this.newScreen = newScreen;
		return this;
	}

	@Override
	public boolean onClick(Screen curScreen) {
		if (newScreen != null) {
			Screen screen = newScreen.get();
			if (screen != null) {
				curScreen.getMinecraft().setScreen(screen);
				return true;
			}
		}
		return false;
	}
	
	
	protected Component name = Component.empty();
	public Tab withName(Component name) {
		Objects.requireNonNull(name);
		this.name = name;
		return this;
	}

	@Override
	public Component getName() {
		return name;
	}
	
	
	protected GuiIcon icon;
	public Tab withIcon(GuiIcon icon) {
		this.icon = icon;
		return this;
	}
	
	@Nullable
	public GuiIcon getIcon() {
		return icon;
	}
	
	@Override
	public void renderIcon(GuiGraphics guiGraphics, int x, int y) {
		GuiIcon icon = getIcon();
		if (icon != null) {
			icon.render(guiGraphics, x, y);
		}
	}
	
}
