package com.github.standobyte.jojo.mechanics.clothes.itemdata;

import com.mojang.serialization.Codec;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

public enum ClothesSlotType implements StringRepresentable {
	HEAD("head"),
	CHEST("chest"),
	LEGS("legs"),
	FEET("feet");
	
	private final String name;
	
	private ClothesSlotType(String name) {
		this.name = name;
	}

	public static final Codec<ClothesSlotType> CODEC = StringRepresentable.fromEnum(ClothesSlotType::values);
	public static final StreamCodec<FriendlyByteBuf, ClothesSlotType> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(ClothesSlotType.class);
	
	@Override
	public String getSerializedName() {
		return name;
	}
}