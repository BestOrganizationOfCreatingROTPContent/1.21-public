package com.github.standobyte.jojo.powersystem.standpower;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.powersystem.standpower.type.StandType;
import com.github.standobyte.jojo.util.NBTUtil;
import com.github.standobyte.jojo.util.network.NetworkUtil;
import com.mojang.datafixers.util.Either;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public class StandInstance {
	private final Either<StandType, ResourceLocation> standType;
	private Optional<ResourceLocation> skin = Optional.empty();
	
	@Nullable
	public static StandInstance fromExistingStandId(ResourceLocation standId) {
		StandType standType = StandType.fromId(standId);
		return standType != null ? new StandInstance(standType) : null;
	}
	
	protected static StandInstance fromStandId(ResourceLocation standId) {
		StandType stand = StandType.fromId(standId);
		return new StandInstance(stand != null ? Either.left(stand) : Either.right(standId));
	}
	
	public StandInstance(@Nonnull StandType standType) {
		this.standType = Either.left(standType);
	}
	
	protected StandInstance(Either<StandType, ResourceLocation> standType) {
		this.standType = standType;
	}
	
	public StandType getStandType() {
		return standType.left().filter(StandType::isEnabled).orElse(null);
	}
	
	public boolean standExists() {
		return standType.left().filter(StandType::isEnabled).isPresent();
	}
	
	protected ResourceLocation getStandId() {
		return standType.map(StandType::getId, Function.identity());
	}
	
	
	@ApiStatus.Internal
	public void setCustomSkin(Optional<ResourceLocation> skin) {
		Objects.requireNonNull(skin);
		this.skin = skin;
	}
	
	public Optional<ResourceLocation> getSelectedSkin() {
		return skin;
	}
	
	
	
	public CompoundTag serializeNBT() {
		CompoundTag nbt = new CompoundTag();
		nbt.put("StandType", StringTag.valueOf(getStandId().toString()));
		skin.ifPresent(skinId -> nbt.put("Skin", StringTag.valueOf(skinId.toString())));
		return nbt;
	}

	public static Optional<StandInstance> fromNBT(CompoundTag nbt) {
		Optional<StandInstance> standOptional = NBTUtil.getResLocOptional(nbt, "StandType")
				.map(StandInstance::fromStandId);
		standOptional.ifPresent(stand -> {
			NBTUtil.getResLocOptional(nbt, "Skin").ifPresent(skinId -> stand.setCustomSkin(Optional.of(skinId)));
		});
		return standOptional;
	}
	
	public static final StreamCodec<FriendlyByteBuf, StandInstance> NETWORK_CODEC = StreamCodec.composite(
			ResourceLocation.STREAM_CODEC, instance -> instance.getStandId(),
			NetworkUtil.optionalCodec(ResourceLocation.STREAM_CODEC), StandInstance::getSelectedSkin,
			(ResourceLocation standId, Optional<ResourceLocation> skin) -> {
				StandInstance stand = fromStandId(standId);
				stand.setCustomSkin(skin);
				return stand;
			});

}
