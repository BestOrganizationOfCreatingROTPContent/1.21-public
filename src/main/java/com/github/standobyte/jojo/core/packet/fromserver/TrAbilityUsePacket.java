package com.github.standobyte.jojo.core.packet.fromserver;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.core.PacketsRegister;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityInput;
import com.github.standobyte.jojo.powersystem.ability.AbilityId.AbilityInputNetwork;
import com.github.standobyte.jojo.powersystem.ability.AbilityInput.InputEventType;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class TrAbilityUsePacket implements CustomPacketPayload {
	private final int entityId;
	private final short key;
	private final InputEventType inputType;
	private final Ability abilityEncode;
	private final AbilityInputNetwork abilityDecoded;
	private final float timeTookToResolve;
	private RegistryFriendlyByteBuf extraData;
	
	public static TrAbilityUsePacket click(int entityId, Ability ability, float timeTookToResolve) {
		return new TrAbilityUsePacket(entityId, (short) 0, InputEventType.PRESS_CLICK, ability, null, timeTookToResolve);
	}
	
	public static TrAbilityUsePacket startHold(int entityId, short key, Ability ability, float timeTookToResolve) {
		return new TrAbilityUsePacket(entityId, key, InputEventType.PRESS_HOLD, ability, null, timeTookToResolve);
	}
	
	public static TrAbilityUsePacket releaseHold(int entityId, short key) {
		return new TrAbilityUsePacket(entityId, key, InputEventType.RELEASE, null, null, 0);
	}
	
	private TrAbilityUsePacket(int entityId, short key, InputEventType inputType, 
			@Nullable Ability abilityEncode, @Nullable AbilityInputNetwork abilityDecoded, float timeTookToResolve) {
		this.entityId = entityId;
		this.key = key;
		this.inputType = inputType;
		this.abilityEncode = abilityEncode;
		this.abilityDecoded = abilityDecoded;
		this.timeTookToResolve = timeTookToResolve;
	}

	
	
	private static CustomPacketPayload.Type<TrAbilityUsePacket> type;
	
	public static class Handler implements PacketsRegister.PacketOGHandler<TrAbilityUsePacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<TrAbilityUsePacket> type() {
			return type;
		}

		@Override
		public void encode(TrAbilityUsePacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeInt(packet.entityId);
			buf.writeShort(packet.key);
			buf.writeEnum(packet.inputType);
			if (packet.inputType != InputEventType.RELEASE) {
				AbilityInputNetwork.encodeInput(buf, packet.abilityEncode, null);
				buf.writeFloat(packet.timeTookToResolve);
				if (packet.abilityEncode != null) {
					packet.abilityEncode.writeExtraInput(buf);
				}	
			}
		}

		@Override
		public TrAbilityUsePacket decode(RegistryFriendlyByteBuf buf) {
			int entityId = buf.readInt();
			short key = buf.readShort();
			InputEventType inputType = buf.readEnum(InputEventType.class);
			return switch (inputType) {
				case RELEASE -> TrAbilityUsePacket.releaseHold(entityId, key);
				default -> {
					AbilityInputNetwork ability = AbilityInputNetwork.decodeInput(buf);
					float timeTookToResolve = buf.readFloat();
					
					TrAbilityUsePacket packet = new TrAbilityUsePacket(entityId, key, inputType, null, ability, timeTookToResolve);
					packet.extraData = buf;
					yield packet;
				}
			};
		}

		@Override
		public void handle(TrAbilityUsePacket payload, IPayloadContext context) {
			Entity entity = ClientProxy.getEntityById(payload.entityId);
			if (entity instanceof LivingEntity living) {
				switch (payload.inputType) {
					case PRESS_CLICK -> {
						Ability ability = payload.abilityDecoded.getAbility(living);
						AbilityInput.click(ability, living, payload.extraData, payload.timeTookToResolve);
					}
					case PRESS_HOLD -> {
						Ability ability = payload.abilityDecoded.getAbility(living);
						AbilityInput.startHolding(payload.key, ability, living, payload.extraData, payload.timeTookToResolve);
					}
					case RELEASE -> {
						AbilityInput.releaseHolding(payload.key, living);
					}
				}
			}
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}
	
}
