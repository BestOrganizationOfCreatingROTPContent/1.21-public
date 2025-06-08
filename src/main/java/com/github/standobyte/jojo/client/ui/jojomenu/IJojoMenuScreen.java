package com.github.standobyte.jojo.client.ui.jojomenu;

import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public interface IJojoMenuScreen {
	public static final int DEFAULT_WIDTH = 230;
	public static final int DEFAULT_HEIGHT = 227;
	public static final ResourceLocation TABS_TEXTURE = JojoMod.resLoc("textures/gui/paper_style/screen_tabs.png");
	
	TabCategory getTabCategory();
	
	Tab getTab();
	
	default int getWindowX(Screen screen) { return (screen.width  - getWindowWidth()) / 2; }
	default int getWindowY(Screen screen) { return (screen.height - getWindowHeight()) / 2; }
	default int getWindowWidth() { return DEFAULT_WIDTH; }
	default int getWindowHeight() { return DEFAULT_HEIGHT; }
	
	public static final int TAB_WIDTH = 28;
	public static final int TAB_LENGTH = 32;
	
	public default void renderTabs(GuiGraphics guiGraphics, Screen screen) {
		int x = getWindowX(screen);
		int y = getWindowY(screen);
		int width = getWindowWidth();
		TabCategory curCategory = getTabCategory();
		Tab curTab = getTab();
		
		int tabX = x - TAB_LENGTH + 4;
		int tabY = y;
		boolean firstTab = true;
		for (TabCategory category : TabCategory.getActiveCategories()) {
			float texX = firstTab ? 0 : TAB_LENGTH;
			float texY = TAB_LENGTH * 2 + (category == curCategory ? TAB_WIDTH : 0);
			guiGraphics.blit(RenderType::guiTextured, TABS_TEXTURE, 
					tabX, tabY, texX, texY, TAB_LENGTH, TAB_WIDTH, 256, 256);
			category.renderIcon(guiGraphics, tabX + 10, tabY + 6);
			tabY += TAB_WIDTH;
			firstTab = false;
		}

		tabX = x + width - 4;
		tabY = y;
		firstTab = true;
		for (Tab tab : curCategory.getActiveTabs()) {
			float texX = TAB_LENGTH * 3 + (firstTab ? 0 : TAB_LENGTH);
			float texY = TAB_LENGTH * 2 + (tab == curTab ? TAB_WIDTH : 0);
			guiGraphics.blit(RenderType::guiTextured, TABS_TEXTURE, 
					tabX, tabY, texX, texY, TAB_LENGTH, TAB_WIDTH, 256, 256);
			tab.renderIcon(guiGraphics, tabX + 6, tabY + 6);
			tabY += TAB_WIDTH;
			firstTab = false;
		}
	}
	
	default void renderTabTooltip(GuiGraphics guiGraphics, Screen screen, int mouseX, int mouseY) {
		IJojoMenuTab tab = getTabAt(mouseX, mouseY, screen);
		if (tab != null) {
			Component name = tab.getName();
			if (name != null) {
				guiGraphics.renderTooltip(screen.getFont(), name, mouseX, mouseY);
			}
		}
	}
	
	default boolean clickTab(double mouseX, double mouseY, int button, Screen screen) {
		if (button == 0) {
			IJojoMenuTab tab = getTabAt(mouseX, mouseY, screen);
			
			if (tab != null) {
				return tab.onClick(screen);
			}
		}
		return false;
	}
	
	@Nullable
	default public IJojoMenuTab getTabAt(double mouseX, double mouseY, Screen screen) {
		int x = getWindowX(screen);
		int y = getWindowY(screen);
		int width = getWindowWidth();
		
		if (mouseY >= y) {
			if (mouseX < x && mouseX >= x - TAB_LENGTH) {
				int categoryIndex = (int) ((mouseY - y) / TAB_WIDTH);
				List<TabCategory> categories = TabCategory.getActiveCategories();
				if (categoryIndex < categories.size()) {
					return categories.get(categoryIndex);
				}
			}
			
			if (mouseX > x + width && mouseX <= x + width + TAB_LENGTH) {
				int tabIndex = (int) ((mouseY - y) / TAB_WIDTH);
				List<Tab> tabs = getTabCategory().getActiveTabs();
				if (tabIndex < tabs.size()) {
					return tabs.get(tabIndex);
				}
			}
		}
		
		return null;
	}
	
	
	public static void onScreenKeyPress() {
		List<TabCategory> categories = TabCategory.getActiveCategories();
		if (!categories.isEmpty()) {
			TabCategory category = categories.get(0);
			Tab tab = category.getActiveTabs().get(0);
			Minecraft.getInstance().setScreen(new PlaceholderScreen(Component.empty(), category, tab));
		}
	}
	
}
