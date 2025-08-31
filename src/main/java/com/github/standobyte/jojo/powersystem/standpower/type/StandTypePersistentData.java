package com.github.standobyte.jojo.powersystem.standpower.type;

import com.github.standobyte.jojo.core.packet.fromserver.TrStandDataPacket;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.network.PacketDistributor;

public class StandTypePersistentData implements INBTSerializable<CompoundTag> {
	protected int resolveReached;
//	public Set<String> unlockedSkills;
	
	public int getResolveReached() {
		return resolveReached;
	}
	
	public void incResolveReached(LivingEntity standUser) {
		++resolveReached;
		syncOnUpdate(standUser);
	}
	
	public void syncOnUpdate(LivingEntity user) {
		if (!user.level().isClientSide()) {
			PacketDistributor.sendToPlayersTrackingEntity(user, new TrStandDataPacket(user.getId(), this, true));
			if (user instanceof ServerPlayer player) {
				PacketDistributor.sendToPlayer(player, new TrStandDataPacket(user.getId(), this, false));
			}
		}
	}

	@Override
	public CompoundTag serializeNBT(Provider provider) {
		CompoundTag nbt = new CompoundTag();
		nbt.putInt("resolveReached", resolveReached);
		return nbt;
	}
	
	@Override
	public void deserializeNBT(Provider provider, CompoundTag nbt) {
		this.resolveReached = nbt.getInt("resolveReached");
	}
	
	public void toBuf(FriendlyByteBuf buf, boolean isSentToTracking) {
		if (!isSentToTracking) {
			buf.writeVarInt(resolveReached);
		}
	}
	
	public void fromBuf(FriendlyByteBuf buf, boolean isSentToTracking) {
		if (!isSentToTracking) {
			resolveReached = buf.readVarInt();
		}
	}
	
}
