package com.github.standobyte.jojo.powersystem.entityaction.netcode;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.core.PacketsRegister;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.jojo.util.network.NetworkUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class TrEntityActionInstancePacket implements CustomPacketPayload {
	private int performerId;
	@Nullable private EntityActionInstance sendAction;
	@Nullable private FriendlyByteBuf receiveActionData;
	
	private TrEntityActionInstancePacket(int performerId, EntityActionInstance action, FriendlyByteBuf actionData) {
		this.performerId = performerId;
		this.sendAction = action;
		this.receiveActionData = actionData;
	}
	
	public TrEntityActionInstancePacket(int performerId, EntityActionInstance action) {
		this(performerId, action, null);
	}
	
	private static TrEntityActionInstancePacket clientRead(int performerId, FriendlyByteBuf actionData) {
		return new TrEntityActionInstancePacket(performerId, null, actionData);
	}
	
	
	
	private static CustomPacketPayload.Type<TrEntityActionInstancePacket> type;
	
	public static class Handler implements PacketsRegister.PacketOGHandler<TrEntityActionInstancePacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<TrEntityActionInstancePacket> type() {
			return type;
		}

		@Override
		public void encode(TrEntityActionInstancePacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeInt(packet.performerId);
			NetworkUtil.writeOptionally(packet.sendAction, buf, (b, a) -> EntityActionInstance.encode(buf, a));
		}

		@Override
		public TrEntityActionInstancePacket decode(RegistryFriendlyByteBuf buf) {
			int performerId = buf.readInt();
			return clientRead(performerId, NetworkUtil.extraPacketData(buf));
		}
		
		@Override
		public void handle(TrEntityActionInstancePacket payload, IPayloadContext context) {
			Entity entity = ClientProxy.getEntityById(payload.performerId);
			if (entity instanceof LivingEntity living) {
				EntityActionInstance action = NetworkUtil.readOptional(payload.receiveActionData, 
						buf -> EntityActionInstance.decode(entity.level(), buf)).orElse(null);
				LivingComponentAction.getComponent(living).setAction(action, SyncType.NO_SYNC);
			}
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
