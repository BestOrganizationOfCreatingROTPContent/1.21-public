package com.github.standobyte.jojo.core.packet.fromserver;

import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.core.PacketsRegister;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ProjectileHighSpeedPacket(int entityId, Vec3 deltaMovement) implements CustomPacketPayload {
	
	private static CustomPacketPayload.Type<ProjectileHighSpeedPacket> type;
	
	public static class Handler implements PacketsRegister.PacketCodecHandler<ProjectileHighSpeedPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<ProjectileHighSpeedPacket> type() {
			return type;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, ProjectileHighSpeedPacket> reader() {
			return STREAM_CODEC;
		}
		
		
		public static final StreamCodec<RegistryFriendlyByteBuf, ProjectileHighSpeedPacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.INT, ProjectileHighSpeedPacket::entityId,
				Vec3.STREAM_CODEC, ProjectileHighSpeedPacket::deltaMovement,
				ProjectileHighSpeedPacket::new);

		@Override
		public void handle(ProjectileHighSpeedPacket payload, IPayloadContext context) {
			Entity entity = ClientProxy.getEntityById(payload.entityId);
			if (entity != null) {
				Vec3 vec = payload.deltaMovement;
				entity.lerpMotion(vec.x, vec.y, vec.z);
			}
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
