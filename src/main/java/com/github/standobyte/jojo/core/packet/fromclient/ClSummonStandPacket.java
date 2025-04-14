package com.github.standobyte.jojo.core.packet.fromclient;

import com.github.standobyte.jojo.core.PacketsRegister;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;
import com.github.standobyte.jojo.util.network.NetworkUtil;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClSummonStandPacket() implements CustomPacketPayload {
	private static CustomPacketPayload.Type<ClSummonStandPacket> type;
	
	public static class Handler implements PacketsRegister.PacketCodecHandler<ClSummonStandPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<ClSummonStandPacket> type() {
			return type;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, ClSummonStandPacket> reader() {
			return STREAM_CODEC;
		}
		
		
		public static final StreamCodec<RegistryFriendlyByteBuf, ClSummonStandPacket> STREAM_CODEC = NetworkUtil.emptyObjectCodec(ClSummonStandPacket::new);

		@Override
		public void handle(ClSummonStandPacket payload, IPayloadContext context) {
			Player player = context.player();
			StandPower standPower = StandPower.get(player);
			if (standPower.hasPower()) {
				StandType standType = standPower.getPowerType();
				standType.toggleSummon(player, standPower);
			}
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}
	
	
	
	public enum PacketType {
		CLICK,
		STARTED_HOLDING,
		RELEASED
	}
}
