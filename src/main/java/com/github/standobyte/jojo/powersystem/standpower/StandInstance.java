package com.github.standobyte.jojo.powersystem.standpower;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
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

	@Nullable
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
	
	
	@Override
	public int hashCode() {
		return Objects.hash(getStandId(), skin);
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj == this || obj instanceof StandInstance other
				&& this.getStandId().equals(other.getStandId())
				&& this.skin.equals(other.skin);
	}
	
	public StandInstance copy() {
		StandInstance stand = new StandInstance(this.standType);
		stand.setCustomSkin(this.skin);
		return stand;
	}
	
	
	@Nullable
	public Component getStandName(boolean clientSide) {
		StandType stand = getStandType();
		Component name = stand.name.get();
		if (clientSide) {
			StandSkin skin = StandSkinsLoader.getInstance().getSkin(this);
			if (skin != null) {
				int color = skin.getColor();
				return name.copy().withColor(color);
			}
		}
		return name;
	}
	
	
	public static final Codec<StandInstance> CODEC = RecordCodecBuilder.create(
			builder -> builder.group(
					ResourceLocation.CODEC.fieldOf("stand_type").forGetter(StandInstance::getStandId),
					ResourceLocation.CODEC.optionalFieldOf("skin").forGetter(StandInstance::getSelectedSkin))
			.apply(builder, 
					(ResourceLocation standId, Optional<ResourceLocation> standSkin) -> {
						StandInstance stand = StandInstance.fromStandId(standId);
						stand.setCustomSkin(standSkin);
						return stand;
					}));
	
	public static final StreamCodec<FriendlyByteBuf, StandInstance> NETWORK_CODEC = StreamCodec.composite(
			ResourceLocation.STREAM_CODEC, instance -> instance.getStandId(),
			ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs::optional), StandInstance::getSelectedSkin,
			(ResourceLocation standId, Optional<ResourceLocation> skin) -> {
				StandInstance stand = fromStandId(standId);
				stand.setCustomSkin(skin);
				return stand;
			});

}
