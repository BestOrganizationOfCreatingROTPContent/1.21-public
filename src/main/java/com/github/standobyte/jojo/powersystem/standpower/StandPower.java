package com.github.standobyte.jojo.powersystem.standpower;

import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.packet.fromserver.TrPowerStandInstancePacket;
import com.github.standobyte.jojo.core.packet.fromserver.TrStandSkinPacket;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;
import com.github.standobyte.jojo.powersystem.standpower.type.SummonedStand;
import com.github.standobyte.jojo.util.NBTUtil;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;

public class StandPower extends Power<StandPower> {
	protected Optional<StandInstance> standInstance = Optional.empty();
	protected SummonedStand summonedStand;
	
	public StandPower(LivingEntity user) {
		super(user);
	}
	
	
	@Override
	public void tick() {
		super.tick();
		if (summonedStand != null) {
			summonedStand.tickStand(getUser(), this);
		}
	}
	
	
	public void setStand(@Nullable StandType stand) {
		setStandInstance(stand != null ? Optional.of(new StandInstance(stand)) : Optional.empty());
	}
	
	public void setStandInstance(Optional<StandInstance> standInstance) {
		StandType oldStand = getPowerType();
		boolean standChanged = standInstance.map(newStand -> oldStand != newStand.getStandType()).orElseGet(() -> oldStand != null);
		if (oldStand != null && standChanged) {
			oldStand.forceUnsummon(user, this);
		}
		this.standInstance = standInstance;
		if (!user.level().isClientSide()) {
			PacketDistributor.sendToPlayersTrackingEntityAndSelf(user, new TrPowerStandInstancePacket(user.getId(), standInstance));
		}
	}

	@Override
	public StandType getPowerType() {
		return standInstance.map(StandInstance::getStandType).orElse(null);
	}
	
	@Override
	public boolean hasPower() {
		return standInstance.filter(StandInstance::standExists).isPresent();
	}
	
	public Optional<StandInstance> getStandInstance() {
		return standInstance;
	}

	@Override
	public PowerClass<StandPower> getPowerClass() {
		return PowerClass.STAND;
	}
	
	
	public SummonedStand getSummonedStand() {
		return summonedStand;
	}
	
	public boolean isSummoned() {
		return summonedStand != null;
	}
	
	public void setSummonedStand(@Nullable SummonedStand summonedStand) {
		this.summonedStand = summonedStand;
		if (summonedStand != null) {
			summonedStand.setUserAndPower(getUser(), this);
			summonedStand.setSelectedSkin(standInstance.flatMap(StandInstance::getSelectedSkin));
		}
	}
	
	
	public void setSelectedSkin(Optional<ResourceLocation> skin) {
		if (standInstance.isPresent()) {
			standInstance.get().setCustomSkin(skin);
			if (!user.level().isClientSide()) {
				PacketDistributor.sendToPlayersTrackingEntityAndSelf(user, new TrStandSkinPacket(user.getId(), getSelectedSkin()));
			}
		}
		if (summonedStand != null) {
			summonedStand.setSelectedSkin(skin);
		}
	}
	
	public Optional<ResourceLocation> getSelectedSkin() {
		if (standInstance.isEmpty()) return Optional.empty();
		return standInstance.get().getSelectedSkin();
	}
	

	@Override
	public void syncToPlayer(ServerPlayer user) {
		super.syncToPlayer(user);
		PacketDistributor.sendToPlayer(user, new TrPowerStandInstancePacket(user.getId(), standInstance));
		PacketDistributor.sendToPlayer(user, new TrStandSkinPacket(user.getId(), getSelectedSkin()));
	}

	@Override
	public void syncToTracking(ServerPlayer player) {
		super.syncToTracking(player);
		PacketDistributor.sendToPlayer(player, new TrPowerStandInstancePacket(user.getId(), standInstance));
		PacketDistributor.sendToPlayer(player, new TrStandSkinPacket(user.getId(), getSelectedSkin()));
	}
	
	@Override
	protected void onPlayerCloneData(StandPower newData, boolean wasDeath) {
		super.onPlayerCloneData(newData, wasDeath);
		newData.standInstance = this.standInstance;
	}
	
	
	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag nbt = super.serializeNBT(provider);
		standInstance.ifPresent(
				stand -> StandInstance.CODEC.encodeStart(NbtOps.INSTANCE, stand)
				.ifSuccess(standNbt -> nbt.put("StandInstance", standNbt)));
		return nbt;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
		super.deserializeNBT(provider, nbt);
		standInstance = NBTUtil.getCompoundOptional(nbt, "StandInstance")
				.flatMap(standNbt -> StandInstance.CODEC.decode(NbtOps.INSTANCE, standNbt).result())
				.map(pair -> pair.getFirst());
	}
	
	
	@Nullable
	public static StandPower get(LivingEntity entity) {
		return PowerClass.STAND.get(entity);
	}
	
	public static Optional<StandPower> getOptional(LivingEntity entity) {
		return PowerClass.STAND.getOptional(entity);
	}

}
