package com.github.standobyte.jojo.core.packet.fromclient;

import com.github.standobyte.jojo.core.PacketsRegister;
import com.github.standobyte.jojo.core.packet.fromserver.TrAimTargetPacket;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.util.StandUtil;
import com.github.standobyte.jojo.util.mc.ActionTarget;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClAimTargetPacket implements CustomPacketPayload {
	private final ActionTarget target;
	private final PacketType entityType;
	
	public ClAimTargetPacket(ActionTarget target, PacketType entityType) {
		this.target = target;
		this.entityType = entityType;
	}
	
	public enum PacketType {
		PLAYER,
		STAND
	}

	
	
	private static CustomPacketPayload.Type<ClAimTargetPacket> type;
	
	public static class Handler implements PacketsRegister.PacketOGHandler<ClAimTargetPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<ClAimTargetPacket> type() {
			return type;
		}

		@Override
		public void encode(ClAimTargetPacket packet, RegistryFriendlyByteBuf buf) {
			packet.target.writeToBuf(buf);
			buf.writeEnum(packet.entityType);
		}

		@Override
		public ClAimTargetPacket decode(RegistryFriendlyByteBuf buf) {
			return new ClAimTargetPacket(ActionTarget.readFromBuf(buf), buf.readEnum(PacketType.class));
		}

		@Override
		public void handle(ClAimTargetPacket payload, IPayloadContext context) {
			Player player = context.player();
			LivingComponentAction entityActionComponent;
			switch (payload.entityType) {
				case PLAYER -> {
					entityActionComponent = LivingComponentAction.getExistingComponent(player);
					if (entityActionComponent != null) {
						entityActionComponent.entityAim.setTargetOnSync(payload.target, player.level());
					}
				}
				case STAND -> {
					StandEntity stand = StandUtil.getSummonedStand(player);
					entityActionComponent = stand != null ? LivingComponentAction.getComponent(stand) : null;
					if (entityActionComponent != null && entityActionComponent.entityAim.setTargetOnSync(payload.target, player.level())) {
						PacketDistributor.sendToPlayersTrackingEntityAndSelf(stand, new TrAimTargetPacket(stand.getId(), payload.target));
					}
				}
			};
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}
	
}
