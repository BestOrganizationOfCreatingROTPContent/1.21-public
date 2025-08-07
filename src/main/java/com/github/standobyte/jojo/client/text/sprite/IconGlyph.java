package com.github.standobyte.jojo.client.text.sprite;

import java.util.function.Function;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.SheetGlyphInfo;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;

public class IconGlyph extends BakedGlyph {

	public IconGlyph(GlyphRenderTypes renderTypes, float u0, float u1, float v0, float v1, 
			float left, float right, float up, float down) {
		super(renderTypes, u0, u1, v0, v1, left, right, up, down);
	}

	public static class Info implements GlyphInfo {
		public GuiIcon icon;
		public float width;
		public float height;
		public float left;
		public float up;
		public float offset;
		
		public Info(GuiIcon icon, float width, float height) {
			this(icon, width, height, 0, (7 - height) / 2f, 1);
		}
		
		public Info(GuiIcon icon, float width, float height, float left, float up, float offset) {
			this.icon = icon;
			this.width = width;
			this.height = height;
			this.left = left;
			this.up = up;
			this.offset = offset;
		}

		@Override
		public float getAdvance() {
			return width + offset;
		}

		@Override
		public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> glyphProvider) {
			GlyphRenderTypes renderTypes = GlyphRenderTypes.createForColorTexture(icon.file);
			return new IconGlyph(renderTypes, 
					icon.minU, icon.minU + icon.widthU, icon.minV, icon.minV + icon.heightV, 
					left, left + width, up, up + height);
		}
	}
	
	public static class Sprites {
		public static Int2ObjectMap<GlyphInfo> glyphs = new Int2ObjectArrayMap<>();
		
		protected static final int _UTF_16_PCA = 0xEAFA; // private code area: 0xE000..0xF8FF, we'll start somewhere in the middle
		public static void add(int index, GlyphInfo info) {
			glyphs.put(index, info);
		}
		
		public static int indexToCharCode(int index) {
			return index + _UTF_16_PCA;
		}
		
		@Nullable
		public static GlyphInfo get(int character) {
			int index = character - _UTF_16_PCA;
			if (index < 0) return null;
			return glyphs.get(index);
		}
	}

}
