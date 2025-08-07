package com.github.standobyte.jojo.client.text.sprite;

import java.util.HashSet;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.mechanics.StoryPart;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;

@ApiStatus.Internal
public class StoryPartIconGlyphs {
	protected static HashSet<ResourceLocation> clientResolvedGlyphs = new HashSet<>();
	protected static final int glyphOffset = 0;
	
	public static void cacheGlyphCode(ResourceLocation icon, StoryPart value) {
		if (clientResolvedGlyphs.add(icon)) {
			value.clientGlyphIndex = clientResolvedGlyphs.size() - 1;
			IconGlyph.Sprites.add(value.clientGlyphIndex + glyphOffset, new IconGlyph.Info(new GuiIcon(icon, 16, 16), 8, 8));
		}
	}
	
	public static int getGlyphCharCode(Holder<StoryPart> holder) {
															if (holder == null) return -1;
		StoryPart value = holder.value();					if (value == null) return -1;
		StoryPart.partIcon(holder); // resolves the icon path, and the glyph code along with it
		return IconGlyph.Sprites.indexToCharCode(value.clientGlyphIndex) + StoryPartIconGlyphs.glyphOffset;
	}
}
