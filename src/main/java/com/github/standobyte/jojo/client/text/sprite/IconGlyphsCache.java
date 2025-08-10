package com.github.standobyte.jojo.client.text.sprite;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.mojang.blaze3d.font.GlyphInfo;

import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2CharMap;
import it.unimi.dsi.fastutil.objects.Object2CharOpenHashMap;
import net.minecraft.resources.ResourceLocation;

public class IconGlyphsCache {
	public static Object2CharMap<ResourceLocation> _iconToPCAUnicode = new Object2CharOpenHashMap<>();
	public static Char2ObjectMap<GlyphInfo> _glyphsByIndex = new Char2ObjectArrayMap<>();

	public static char getOrComputeCharCode(IconGlyphInfo glyph) {
		return _iconToPCAUnicode.computeIfAbsent(glyph.icon.file, __ -> {
			char index = (char) _iconToPCAUnicode.size();
			_glyphsByIndex.put(index, glyph);
			return indexToCharCode(index);
		});
	}


	protected static final char _UTF_16_PCA = 0xEAFA; // private code area: 0xE000..0xF8FF, we'll start somewhere in the middle

	public static char indexToCharCode(char index) {
		return (char) (index + _UTF_16_PCA);
	}

	@ApiStatus.Internal
	@Nullable
	public static GlyphInfo get(char character) {
		int index = character - _UTF_16_PCA;
		if (index < 0) return null;
		return _glyphsByIndex.get((char) index);
	}
}
