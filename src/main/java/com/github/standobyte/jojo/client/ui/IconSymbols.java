package com.github.standobyte.jojo.client.ui;

import com.github.standobyte.jojo.client.text.sprite.IconGlyphInfo;
import com.github.standobyte.jojo.client.text.sprite.IconGlyphsCache;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.core.JojoMod;

public class IconSymbols {
	// they are kinda small though
	public static final char LMB_CLICK = IconGlyphsCache.makeCharCodeFor(new IconGlyphInfo(
			new GuiIcon(JojoMod.resLoc("textures/gui/sprites/left_click.png"), 3, 0, 10, 16, 16, 16), 5, 8));
	public static final char RMB_CLICK = IconGlyphsCache.makeCharCodeFor(new IconGlyphInfo(
			new GuiIcon(JojoMod.resLoc("textures/gui/sprites/right_click.png"), 3, 0, 10, 16, 16, 16), 5, 8));
	public static final char MMB_CLICK = IconGlyphsCache.makeCharCodeFor(new IconGlyphInfo(
			new GuiIcon(JojoMod.resLoc("textures/gui/sprites/middle_click.png"), 3, 0, 10, 16, 16, 16), 5, 8));
}
