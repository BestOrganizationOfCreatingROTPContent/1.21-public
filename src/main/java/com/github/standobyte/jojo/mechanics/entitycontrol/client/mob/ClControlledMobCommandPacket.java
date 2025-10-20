package com.github.standobyte.jojo.mechanics.entitycontrol.client.mob;

import com.github.standobyte.jojo.core.PacketsRegister;
import com.github.standobyte.jojo.mechanics.entitycontrol.ServerEntityController;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClControlledMobCommandPacket(CommandType commandType, int slot) implements CustomPacketPayload {
	private static CustomPacketPayload.Type<ClControlledMobCommandPacket> type;
	
	public enum CommandType {
		PICK_SLOT,
		EMPTY_MAIN_HAND,
		SWAP_ITEMS,
		TOSS,
		WITCH_PICK_DRINK_POTION,
		WITCH_PICK_SPLASH_POTION,
	}

	public static class Handler implements PacketsRegister.PacketOGHandler<ClControlledMobCommandPacket> {

		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<ClControlledMobCommandPacket> type() {
			return type;
		}
		
		@Override
		public void encode(ClControlledMobCommandPacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeEnum(packet.commandType);
			buf.writeVarInt(packet.slot);
		}
		
		@Override
		public ClControlledMobCommandPacket decode(RegistryFriendlyByteBuf buf) {
			CommandType commandType = buf.readEnum(CommandType.class);
			int slot = buf.readVarInt();
			return new ClControlledMobCommandPacket(commandType, slot);
		}

		@Override
		public void handle(ClControlledMobCommandPacket packet, IPayloadContext context) {
			ServerPlayer player = (ServerPlayer) context.player();
			
			Entity curControlTarget = ServerEntityController.getControlTarget(player);
			if (curControlTarget instanceof LivingEntity living) {
				HardcodedMobControlCommands.onHotbarPacket(living, packet.commandType, packet.slot);
			}
		}

	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
