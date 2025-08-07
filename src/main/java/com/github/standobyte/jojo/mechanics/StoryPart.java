package com.github.standobyte.jojo.mechanics;

import com.github.standobyte.jojo.client.text.sprite.IconGlyph;
import com.github.standobyte.jojo.client.text.sprite.IconGlyphsCache;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.core.JojoRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

public class StoryPart {
	protected final TextColor nameColor;
	protected Component name;
	protected ResourceLocation icon;
	protected int clientGlyphIndex = 0;

	public StoryPart(TextColor nameColor) {
		this.nameColor = nameColor;
	}


	public static Component partName(Holder<StoryPart> holder) {
		if (holder == null) return CommonComponents.EMPTY;
		StoryPart value = holder.value();					if (value == null) return CommonComponents.EMPTY;
		ResourceKey<StoryPart> key = holder.getKey();		if (key == null) return CommonComponents.EMPTY;

		if (value.name == null) {
			MutableComponent iconAndName = null;
			if (FMLEnvironment.dist == Dist.CLIENT) {
				ResourceLocation icon = StoryPart.partIcon(holder);
				if (icon != null && value.clientGlyphIndex > 0) {
					iconAndName = Component.literal(Character.toString(value.clientGlyphIndex));
				}
			}
			if (iconAndName == null) {
				iconAndName = Component.empty();
			}

			ResourceLocation id = key.location();
			String tlKey = id.getNamespace() + ".story_part." + id.getPath();
			MutableComponent name = Component.translatable(tlKey);
			name.withStyle(style -> style.withColor(value.nameColor));
			iconAndName.append(name);

			value.name = iconAndName;
		}
		return value.name;
	}

	public static ResourceLocation partIcon(Holder<StoryPart> holder) {
		if (holder == null) return null;
		StoryPart value = holder.value();					if (value == null) return null;
		ResourceKey<StoryPart> key = holder.getKey();		if (key == null) return null;

		if (value.icon == null) {
			ResourceLocation id = key.location();
			value.icon = id.withPath(path -> "textures/story_part/" + path + ".png");
			if (FMLEnvironment.dist == Dist.CLIENT) {
				value.clientGlyphIndex = IconGlyphsCache.getOrComputeCharCode(
						new IconGlyph.Info(new GuiIcon(value.icon, 16, 16), 8, 8));
			}
		}
		return value.icon;
	}


	public static final Codec<StoryPart> DIRECT_CODEC = RecordCodecBuilder.create(
			builder -> builder.group(
					TextColor.CODEC.optionalFieldOf("name_color", TextColor.fromRgb(0xFFFFFF)).forGetter(set -> set.nameColor))
			.apply(builder, StoryPart::new));


	public static final Codec<Holder<StoryPart>> REG_CODEC = RegistryFixedCodec.create(JojoRegistries.STORY_PARTS_REG_KEY);
}
