package com.github.standobyte.jojo.mechanics.entitycontrol.tmp;

import com.github.standobyte.jojo.core.PacketsRegister;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.mechanics.entitycontrol.ServerEntityController;
import com.github.standobyte.jojo.util.entitycomponent.ComponentUtil;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClQuitControllerPacket implements CustomPacketPayload {
	private static CustomPacketPayload.Type<ClQuitControllerPacket> type;
	
	private static final ClQuitControllerPacket INSTANCE = new ClQuitControllerPacket();
	public static ClQuitControllerPacket packet() {
		return INSTANCE;
	}
	
	private ClQuitControllerPacket() {}

	public static class Handler implements PacketsRegister.PacketCodecHandler<ClQuitControllerPacket> {

		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<ClQuitControllerPacket> type() {
			return type;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, ClQuitControllerPacket> reader() {
			return STREAM_CODEC;
		}


		public static final StreamCodec<RegistryFriendlyByteBuf, ClQuitControllerPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

		@Override
		public void handle(ClQuitControllerPacket packet, IPayloadContext context) {
			ServerPlayer player = (ServerPlayer) context.player();
			ServerEntityController component = ComponentUtil.getExistingDataOrNull(player, ModDataAttachmentTypes.CONTROLLER);
			if (component != null) {
				component.stopControlling();
			}
		}

	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
