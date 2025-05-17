package com.github.standobyte.jojo.mechanics.clothes.itemdata;

import java.util.Objects;
import java.util.Optional;

import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesPiece.SubClothingPiece;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

//TODO (!!!!!) (clothes) use the ClothesDataComponent
/*
 * [V]	item model
 * [V]	item name
 * [V]	humanoid layer model
 * [_]	equip sound
 */
public class ClothesDataComponent {
	private final Holder<ClothesSet> set;
	private final ClothesSlotType slot;
	private final Optional<ClothesPiece.SubClothingPiece> subPiece;

	private final Holder<StoryCharacter> character;
	private final ClothesPiece piece;
	
	public ClothesDataComponent(
			Holder<ClothesSet> set,
			ClothesSlotType slot, 
			Optional<ClothesPiece.SubClothingPiece> subPiece) {
		this.set = set;
		this.slot = slot;
		this.subPiece = subPiece;
		
		this.character = set.value().getCharacter();
		ClothesPiece piece = set.value().getPiece(slot);
		this.piece = subPiece.map(piece::getSubPiece).orElse(piece);
	}
	
	public Holder<ClothesSet> getClothesSet() {
		return set;
	}
	
	public ClothesSlotType getSlot() {
		return slot;
	}
	
	public ClothesPiece getPiece() {
		return piece;
	}
	
	
	@Override
	public int hashCode() {
		return Objects.hash(set, slot, subPiece);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else {
			return obj instanceof ClothesDataComponent other
				&& this.set.equals(other.set)
				&& this.slot == other.slot
				&& this.subPiece.equals(other.subPiece);
		}
	}
	

	public static final Codec<ClothesDataComponent> CODEC = RecordCodecBuilder.create(
			builder -> builder.group(
					ClothesSet.REG_CODEC.fieldOf("set").forGetter(ClothesDataComponent::getClothesSet),
					ClothesSlotType.CODEC.fieldOf("slot").forGetter(ClothesDataComponent::getSlot),
					ClothesPiece.SubClothingPiece.CODEC.optionalFieldOf("sub_piece").forGetter(component -> component.subPiece))
			.apply(builder, ClothesDataComponent::new));
	
	public static final StreamCodec<RegistryFriendlyByteBuf, ClothesDataComponent> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.holderRegistry(JojoRegistries.CLOTHES_SETS_REG_KEY), ClothesDataComponent::getClothesSet,
			ClothesSlotType.STREAM_CODEC, ClothesDataComponent::getSlot,
			SubClothingPiece.STREAM_CODEC.apply(ByteBufCodecs::optional), component -> component.subPiece,
			ClothesDataComponent::new);
	
}