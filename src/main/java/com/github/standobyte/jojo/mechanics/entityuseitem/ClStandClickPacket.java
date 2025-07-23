package com.github.standobyte.jojo.mechanics.entityuseitem;

import com.github.standobyte.jojo.core.PacketsRegister;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.util.StandUtil;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClStandClickPacket(HitResultSync target, InteractionHand... hand) implements CustomPacketPayload {
	private static CustomPacketPayload.Type<ClStandClickPacket> type;
	
	public ClStandClickPacket(HitResult target, InteractionHand... hand) {
		this(new HitResultSync(target), hand);
	}

	public static class Handler implements PacketsRegister.PacketOGHandler<ClStandClickPacket> {

		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<ClStandClickPacket> type() {
			return type;
		}

		@Override
		public void encode(ClStandClickPacket packet, RegistryFriendlyByteBuf buf) {
			HitResultSync.STREAM_CODEC.encode(buf, packet.target);
			buf.writeVarInt(packet.hand.length);
			for (InteractionHand hand : packet.hand) {
				buf.writeEnum(hand);
			}
		}
		
		@Override
		public ClStandClickPacket decode(RegistryFriendlyByteBuf buf) {
			HitResultSync target = HitResultSync.STREAM_CODEC.decode(buf);
			InteractionHand[] hand = new InteractionHand[buf.readVarInt()];
			for (int i = 0; i < hand.length; i++) {
				hand[i] = buf.readEnum(InteractionHand.class);
			}
			return new ClStandClickPacket(target, hand);
		}

		@Override
		public void handle(ClStandClickPacket packet, IPayloadContext context) {
			ServerPlayer player = (ServerPlayer) context.player();
			StandEntity standEntity = StandUtil.getSummonedStand(player);
			if (standEntity != null) {
				HitResult target = packet.target().resolveEntity(standEntity.level());
				ServerSideLivingClick.rightClick(standEntity, player, target);
			}
		}
		
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}
	
}
