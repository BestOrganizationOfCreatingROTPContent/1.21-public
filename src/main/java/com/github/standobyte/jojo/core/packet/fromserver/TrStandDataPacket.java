package com.github.standobyte.jojo.core.packet.fromserver;

import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.core.PacketsRegister;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.type.StandTypePersistentData;
import com.github.standobyte.jojo.util.network.NetworkUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class TrStandDataPacket implements CustomPacketPayload {
	private final int entityId;
	private boolean isSentToTracking;
	private StandTypePersistentData serverPerStandTypeData;
	private FriendlyByteBuf clientPerStandTypeData;
	
	public TrStandDataPacket(int entityId, StandTypePersistentData playerStandTypeData, boolean isSentToTracking) {
		this.entityId = entityId;
		this.isSentToTracking = isSentToTracking;
		this.serverPerStandTypeData = playerStandTypeData;
	}
	
	private TrStandDataPacket(int entityId, boolean isSentToTracking) {
		this.entityId = entityId;
		this.isSentToTracking = isSentToTracking;
	}
	
	
	private static CustomPacketPayload.Type<TrStandDataPacket> type;
	
	public static class Handler implements PacketsRegister.PacketOGHandler<TrStandDataPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<TrStandDataPacket> type() {
			return type;
		}

		@Override
		public void encode(TrStandDataPacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeInt(packet.entityId);
			buf.writeBoolean(packet.isSentToTracking);
			if (packet.serverPerStandTypeData != null) {
				packet.serverPerStandTypeData.toBuf(buf, packet.isSentToTracking);
			}
		}

		@Override
		public TrStandDataPacket decode(RegistryFriendlyByteBuf buf) {
			TrStandDataPacket packet = new TrStandDataPacket(
					buf.readInt(), 
					buf.readBoolean());
			packet.clientPerStandTypeData = NetworkUtil.extraPacketData(buf);
			return packet;
		}

		@Override
		public void handle(TrStandDataPacket payload, IPayloadContext context) {
			Entity entity = ClientProxy.getEntityById(payload.entityId);
			if (entity instanceof LivingEntity living) {
				StandPower standPower = StandPower.get(living);
				if (standPower != null) {
					var perTypePlayerData = standPower.getCurTypeData();
					if (perTypePlayerData != null) {
						perTypePlayerData.fromBuf(payload.clientPerStandTypeData, payload.isSentToTracking);
					}
				}
			}
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
