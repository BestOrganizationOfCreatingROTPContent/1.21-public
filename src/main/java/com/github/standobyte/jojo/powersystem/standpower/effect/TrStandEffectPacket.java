package com.github.standobyte.jojo.powersystem.standpower.effect;

import java.util.Optional;

import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.core.PacketsRegister;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.util.network.NetworkUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class TrStandEffectPacket implements CustomPacketPayload {
	private final PacketType packetType;
	private final int userId;
	private final int effectId;
	private final int targetId;
	private final StandEffectType<?> effectFactory;
	private final StandEffectInstance effect;
	private final boolean isUser;
	private final FriendlyByteBuf buf;

	public static TrStandEffectPacket add(StandEffectInstance effect, boolean sentToOwner) {
		return new TrStandEffectPacket(PacketType.ADD, effect.getStandUser().getId(), effect.getId(), 
				Optional.ofNullable(effect.getTarget()).map(Entity::getId).orElse(-1), effect.effectType, effect, sentToOwner, 
				null);
	}

	public static TrStandEffectPacket remove(StandEffectInstance effect) {
		return new TrStandEffectPacket(PacketType.REMOVE, effect.getStandUser().getId(), effect.getId(), 
				-1, null, null, false, 
				null);
	}

	public static TrStandEffectPacket updateTarget(StandEffectInstance effect) {
		return new TrStandEffectPacket(PacketType.UPDATE_TARGET, effect.getStandUser().getId(), effect.getId(), 
				Optional.ofNullable(effect.getTarget()).map(Entity::getId).orElse(-1), 
				null, null, false, 
				null);
	}

	private TrStandEffectPacket(PacketType packetType, int userId, int effectId, int targetId, 
			StandEffectType<?> effectFactory, StandEffectInstance effect, boolean isUser, FriendlyByteBuf buf) {
		this.packetType = packetType;
		this.userId = userId;
		this.effectId = effectId;
		this.targetId = targetId;
		this.effectFactory = effectFactory;
		this.effect = effect;
		this.isUser = isUser;
		this.buf = buf;
	}



	private static CustomPacketPayload.Type<TrStandEffectPacket> type;

	public static class Handler implements PacketsRegister.PacketOGHandler<TrStandEffectPacket> {

		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<TrStandEffectPacket> type() {
			return type;
		}

		@Override
		public void encode(TrStandEffectPacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeEnum(packet.packetType);
			switch (packet.packetType) {
				case ADD:
					buf.writeInt(packet.userId);
					buf.writeInt(packet.effectId);
					buf.writeInt(packet.targetId);
					NetworkUtil.registryCodec(JojoRegistries.STAND_EFFECTS_REG_KEY).encode(buf, packet.effectFactory);
					buf.writeBoolean(packet.isUser);
	
					buf.writeVarInt(packet.effect.tickCount);
					packet.effect.writeAdditionalPacketData(buf, packet.isUser);
					break;
				case REMOVE:
					buf.writeInt(packet.userId);
					buf.writeInt(packet.effectId);
					break;
				case UPDATE_TARGET:
					buf.writeInt(packet.userId);
					buf.writeInt(packet.effectId);
					buf.writeInt(packet.targetId);
					break;
			}
		}

		@Override
		public TrStandEffectPacket decode(RegistryFriendlyByteBuf buf) {
			PacketType packetType = buf.readEnum(PacketType.class);
			return switch (packetType) {
				case ADD -> {
					int userId = buf.readInt();
					int effectId = buf.readInt();
					int targetId = buf.readInt();
					StandEffectType<?> effectFactory = NetworkUtil.registryCodec(JojoRegistries.STAND_EFFECTS_REG_KEY).decode(buf);
					boolean isUser = buf.readBoolean();
					yield new TrStandEffectPacket(packetType, userId, effectId, targetId, 
							effectFactory, null, isUser, NetworkUtil.extraPacketData(buf));
				}
				case REMOVE -> {
					int userId = buf.readInt();
					int effectId = buf.readInt();
					yield new TrStandEffectPacket(packetType, userId, effectId, -1, 
							null, null, false, null);
				}
				case UPDATE_TARGET -> {
					int userId = buf.readInt();
					int effectId = buf.readInt();
					int targetId = buf.readInt();
					yield new TrStandEffectPacket(packetType, userId, effectId, targetId, 
							null, null, false, null);
				}
			};
		}

		@Override
		public void handle(TrStandEffectPacket payload, IPayloadContext context) {
			Entity entity = ClientProxy.getEntityById(payload.userId);
			if (entity instanceof LivingEntity livingEntity) {
				StandPower stand = StandPower.get(livingEntity);
				if (stand != null) {
					switch (payload.packetType) {
					case ADD:
						StandEffectInstance newEffect = payload.effectFactory.create(entity.level()).withId(payload.effectId).withStand(stand);
						if (payload.targetId != -1) {
							newEffect.withTargetEntityId(payload.targetId);
						}

						newEffect.tickCount = payload.buf.readVarInt();
						newEffect.readAdditionalPacketData(payload.buf, payload.isUser);
						newEffect.updateTarget(entity.level());
						stand.userStandEffects.addEffect(newEffect);
						break;
					case REMOVE:
						UserStandEffects effects = stand.userStandEffects;
						effects.removeEffect(effects.getById(payload.effectId));
					case UPDATE_TARGET:
						StandEffectInstance effect = stand.userStandEffects.getById(payload.effectId);
						if (effect != null) {
							effect.withTargetEntityId(payload.targetId);
						}
					}
				}
			}
		}
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

	private enum PacketType {
		ADD,
		REMOVE,
		UPDATE_TARGET
	}
}
