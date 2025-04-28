package com.github.standobyte.jojo.mechanics.clothes.itemdata;

import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.JojoRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFixedCodec;

public class ClothesSet {
	protected final Holder<StoryCharacter> character;
	protected final Map<ClothesSlotType, ClothesPiece> pieces;
	
	public ClothesSet(Holder<StoryCharacter> character, Map<ClothesSlotType, ClothesPiece> clothesPieces) {
		this.character = character;
		this.pieces = clothesPieces;
	}
	
	public Holder<StoryCharacter> getCharacter() {
		return character;
	}
	
	@Nullable
	public ClothesPiece getPiece(ClothesSlotType slot) { 
		return pieces.get(slot);
	}
	
	
	public static final Codec<ClothesSet> DIRECT_CODEC = RecordCodecBuilder.create(
			builder -> builder.group(
					StoryCharacter.REG_CODEC.fieldOf("character").forGetter(set -> set.character),
					Codec.unboundedMap(ClothesSlotType.CODEC, ClothesPiece.CODEC).fieldOf("pieces").forGetter(set -> set.pieces))
			.apply(builder, ClothesSet::new));

	public static final Codec<Holder<ClothesSet>> REG_CODEC = RegistryFixedCodec.create(JojoRegistries.CLOTHES_SET_REG_KEY);
	
}