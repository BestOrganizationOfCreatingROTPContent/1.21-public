package com.github.standobyte.jojo.mechanics.clothes.itemdata;

import java.util.Optional;

import com.github.standobyte.jojo.core.JojoRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceLocation;

public class StoryCharacter {
	protected final Optional<ResourceLocation> stand;
	
	public StoryCharacter(Optional<ResourceLocation> stand) {
		this.stand = stand;
	}
	
	
	public static final Codec<StoryCharacter> DIRECT_CODEC = RecordCodecBuilder.create(
			builder -> builder.group(
					ResourceLocation.CODEC.optionalFieldOf("stand").forGetter(set -> set.stand))
			.apply(builder, StoryCharacter::new));
	
	public static final Codec<Holder<StoryCharacter>> REG_CODEC = RegistryFixedCodec.create(JojoRegistries.STORY_CHARACTERS_REG_KEY);
	
}