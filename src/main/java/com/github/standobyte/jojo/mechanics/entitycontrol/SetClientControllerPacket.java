package com.github.standobyte.jojo.mechanics.entitycontrol;

import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.core.PacketsRegister;
import com.github.standobyte.jojo.mechanics.entitycontrol.client.ClientEntityController;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetClientControllerPacket(int targetId, String controllerType) implements CustomPacketPayload {
	private static CustomPacketPayload.Type<SetClientControllerPacket> type;

	public static class Handler implements PacketsRegister.PacketCodecHandler<SetClientControllerPacket> {

		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<SetClientControllerPacket> type() {
			return type;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, SetClientControllerPacket> reader() {
			return STREAM_CODEC;
		}


		public static final StreamCodec<RegistryFriendlyByteBuf, SetClientControllerPacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.INT, SetClientControllerPacket::targetId,
				ByteBufCodecs.STRING_UTF8, SetClientControllerPacket::controllerType,
				SetClientControllerPacket::new);

		@Override
		public void handle(SetClientControllerPacket packet, IPayloadContext context) {
			Entity target = ClientProxy.getEntityById(packet.targetId);
			if (target == null) {
				ClientEntityController.setInstance(null);
			}
			else {
				ClientEntityController.onPacket(packet, target);
			}
		}

	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
