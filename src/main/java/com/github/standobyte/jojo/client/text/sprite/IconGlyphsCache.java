package com.github.standobyte.jojo.client.text.sprite;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.mojang.blaze3d.font.GlyphInfo;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.resources.ResourceLocation;

public class IconGlyphsCache {
	public static Object2IntMap<ResourceLocation> _iconToPCAUnicode = new Object2IntOpenHashMap<>();
	public static Int2ObjectMap<GlyphInfo> _glyphsByIndex = new Int2ObjectArrayMap<>();

	public static int getOrComputeCharCode(IconGlyph.Info glyph) {
		return _iconToPCAUnicode.computeIfAbsent(glyph.icon.file, __ -> {
			int index = _iconToPCAUnicode.size();
			_glyphsByIndex.put(index, glyph);
			return indexToCharCode(index);
		});
	}


	protected static final int _UTF_16_PCA = 0xEAFA; // private code area: 0xE000..0xF8FF, we'll start somewhere in the middle

	public static int indexToCharCode(int index) {
		return index + _UTF_16_PCA;
	}

	@ApiStatus.Internal
	@Nullable
	public static GlyphInfo get(int character) {
		int index = character - _UTF_16_PCA;
		if (index < 0) return null;
		return _glyphsByIndex.get(index);
	}
}
