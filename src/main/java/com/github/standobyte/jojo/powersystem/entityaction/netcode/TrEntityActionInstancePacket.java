package com.github.standobyte.jojo.powersystem.entityaction.netcode;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.core.PacketsRegister;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.util.network.NetworkUtil;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TrEntityActionInstancePacket(int performerId, int powerUserId, @Nullable EntityActionInstance action) implements CustomPacketPayload {
	private static CustomPacketPayload.Type<TrEntityActionInstancePacket> type;
	
	public static class Handler implements PacketsRegister.PacketCodecHandler<TrEntityActionInstancePacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<TrEntityActionInstancePacket> type() {
			return type;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, TrEntityActionInstancePacket> reader() {
			return STREAM_CODEC;
		}
		
		
		public static final StreamCodec<RegistryFriendlyByteBuf, TrEntityActionInstancePacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.INT, TrEntityActionInstancePacket::performerId,
				ByteBufCodecs.INT, TrEntityActionInstancePacket::powerUserId,
				NetworkUtil.nullableCodec(EntityActionInstance.NETWORK_CODEC), TrEntityActionInstancePacket::action,
				TrEntityActionInstancePacket::new);

		@Override
		public void handle(TrEntityActionInstancePacket payload, IPayloadContext context) {
			Entity entity = ClientProxy.getEntityById(payload.performerId);
			if (entity instanceof LivingEntity living) {
				LivingEntity user = ClientProxy.getEntityById(payload.powerUserId) instanceof LivingEntity living2 ? living2 : null;
				LivingComponentAction.getComponent(living).setAction(payload.action, user, SyncType.NO_SYNC);
			}
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
