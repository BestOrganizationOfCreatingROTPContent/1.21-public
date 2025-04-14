package com.github.standobyte.jojo.powersystem.playerpower;

import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.core.packet.fromserver.TrPowerTypePacket;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.util.NBTUtil;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;

public class PlayerPower extends Power<PlayerPower> {
	protected Optional<PowerData> powerData = Optional.empty();

	public PlayerPower(LivingEntity user) {
		super(user);
	}

	@Override
	public PlayerPowerType<?> getPowerType() {
		return powerData.map(PowerData::getType).orElse(null);
	}
	
	public void setPowerType(@Nullable PlayerPowerType<?> type) {
		if (getPowerType() != type) {
			initPowerTypeData(type);
			if (!user.level().isClientSide()) {
				PacketDistributor.sendToPlayersTrackingEntityAndSelf(user, new TrPowerTypePacket(user.getId(), type));
			}
		}
	}
	
	protected void initPowerTypeData(@Nullable PlayerPowerType<?> type) {
		powerData = type != null ? Optional.ofNullable(type.newDataInstance()) : Optional.empty();
	}
	
	@Override
	public boolean hasPower() {
		return powerData.isPresent();
	}
	
	@SuppressWarnings("unchecked")
	public <T extends PlayerPowerType<D>, D extends PowerData> Optional<D> getTypeSpecificData(@Nullable T requiredType) {
		return (Optional<D>) powerData.filter(data -> requiredType == null || requiredType == data.getType());
	}
	
	@Override
	public PowerClass<PlayerPower> getPowerClass() {
		return PowerClass.PLAYER_POWER;
	}


	@Override
	public void syncToPlayer(ServerPlayer user) {
		super.syncToPlayer(user);
		PacketDistributor.sendToPlayer(user, new TrPowerTypePacket(user.getId(), getPowerType()));
		powerData.ifPresent(data -> data.syncToPlayer(user));
	}

	@Override
	public void syncToTracking(ServerPlayer player) {
		super.syncToTracking(player);
		PacketDistributor.sendToPlayer(player, new TrPowerTypePacket(user.getId(), getPowerType()));
		powerData.ifPresent(data -> data.syncToTracking(player));
	}
	
	@Override
	public void onPlayerCloneData(PlayerPower newData, boolean wasDeath) {
		super.onPlayerCloneData(newData, wasDeath);
		newData.powerData = this.powerData;
	}
	
	
	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag nbt = super.serializeNBT(provider);
		powerData.ifPresent(data -> {
			nbt.putString("PowerType", data.getType().getId().toString());
			nbt.put("PowerData", data.serializeNBT(provider));
		});
		return nbt;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
		super.deserializeNBT(provider, nbt);
		PlayerPowerType<?> powerType = JojoRegistries.PLAYER_POWER_TYPES_REG.getValue(
				ResourceLocation.parse(nbt.getString("PowerType")));
		initPowerTypeData(powerType);
		powerData.ifPresent(data -> NBTUtil.getCompoundOptional(nbt, "PowerData").ifPresent(
				dataNbt -> data.deserializeNBT(provider, dataNbt)));
	}
	
	
	@Nullable
	public static PlayerPower get(LivingEntity entity) {
		return PowerClass.PLAYER_POWER.get(entity);
	}
	
	public static Optional<PlayerPower> getOptional(LivingEntity entity) {
		return PowerClass.PLAYER_POWER.getOptional(entity);
	}

}
