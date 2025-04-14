package com.github.standobyte.jojo.core.packet.fromserver;

import java.util.Optional;

import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.core.PacketsRegister;
import com.github.standobyte.jojo.powersystem.standpower.StandInstance;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.util.network.NetworkUtil;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TrPowerStandInstancePacket(int entityId, Optional<StandInstance> standInstance) implements CustomPacketPayload {
	private static CustomPacketPayload.Type<TrPowerStandInstancePacket> type;
	
	public static class Handler implements PacketsRegister.PacketCodecHandler<TrPowerStandInstancePacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<TrPowerStandInstancePacket> type() {
			return type;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, TrPowerStandInstancePacket> reader() {
			return STREAM_CODEC;
		}
		
		
		public static final StreamCodec<RegistryFriendlyByteBuf, TrPowerStandInstancePacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.INT, TrPowerStandInstancePacket::entityId,
				NetworkUtil.optionalCodec(StandInstance.NETWORK_CODEC), TrPowerStandInstancePacket::standInstance,
				TrPowerStandInstancePacket::new);

		@Override
		public void handle(TrPowerStandInstancePacket payload, IPayloadContext context) {
			Entity entity = ClientProxy.getEntityById(payload.entityId);
			if (entity instanceof LivingEntity living) {
				StandPower.getOptional(living).ifPresent(power -> power.setStandInstance(payload.standInstance));
			}
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
