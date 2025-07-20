package com.github.standobyte.jojo.core.packet.fromclient;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.PacketsRegister;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityInput;
import com.github.standobyte.jojo.powersystem.ability.AbilityId.AbilityInputNetwork;
import com.github.standobyte.jojo.powersystem.ability.AbilityInput.InputEventType;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClAbilityInputPacket implements CustomPacketPayload {
	private final short key;
	private final InputEventType inputType;
	private final Ability abilityEncode;
	private final AbilityInputNetwork abilityDecoded;
	private final float timeTookToResolve;

	private final LivingEntity clUser;
	private Power<?> clUserPower; // is used to optimize the packets - if the player has power of the same powerClass and powerTypeId as in the abilityId, we don't have to send powerTypeId
	private FriendlyByteBuf extraData;
	
	public static ClAbilityInputPacket click(LivingEntity user, Power<?> power, Ability ability, float timeTookToResolve) {
		return new ClAbilityInputPacket((short) 0, InputEventType.PRESS_CLICK, user, power, ability, null, timeTookToResolve);
	}
	
	public static ClAbilityInputPacket startHold(short key, LivingEntity user, Power<?> power, Ability ability, float timeTookToResolve) {
		return new ClAbilityInputPacket(key, InputEventType.PRESS_HOLD, user, power, ability, null, timeTookToResolve);
	}
	
	public static ClAbilityInputPacket releaseHold(short key) {
		return new ClAbilityInputPacket(key, InputEventType.RELEASE, null, null, null, null, 0);
	}
	
	private ClAbilityInputPacket(short key, InputEventType inputType, LivingEntity user, Power<?> userPower, 
			@Nullable Ability abilityEncode, @Nullable AbilityInputNetwork abilityDecoded, float timeTookToResolve) {
		this.key = key;
		this.inputType = inputType;
		this.clUser = user;
		this.clUserPower = userPower;
		this.abilityEncode = abilityEncode;
		this.abilityDecoded = abilityDecoded;
		this.timeTookToResolve = timeTookToResolve;
	}

	
	
	private static CustomPacketPayload.Type<ClAbilityInputPacket> type;
	
	public static class Handler implements PacketsRegister.PacketOGHandler<ClAbilityInputPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<ClAbilityInputPacket> type() {
			return type;
		}

		@Override
		public void encode(ClAbilityInputPacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeShort(packet.key);
			buf.writeEnum(packet.inputType);
			if (packet.inputType != InputEventType.RELEASE) {
				AbilityInputNetwork.encodeInput(buf, packet.abilityEncode, packet.clUserPower);
				if (packet.abilityEncode != null) {
					buf.writeFloat(packet.timeTookToResolve);
					packet.abilityEncode.writeExtraInput(buf, packet.clUser, true);
				}	
			}
		}

		@Override
		public ClAbilityInputPacket decode(RegistryFriendlyByteBuf buf) {
			short key = buf.readShort();
			InputEventType inputType = buf.readEnum(InputEventType.class);
			return switch (inputType) {
				case RELEASE -> ClAbilityInputPacket.releaseHold(key);
				default -> {
					AbilityInputNetwork ability = AbilityInputNetwork.decodeInput(buf);
					float timeTookToResolve = ability != null ? buf.readFloat() : 0;
					
					ClAbilityInputPacket packet = new ClAbilityInputPacket(key, inputType, null, null, null, ability, timeTookToResolve);
					int extraInputBytes = buf.readableBytes();
					packet.extraData = extraInputBytes > 0 ? new FriendlyByteBuf(buf.readBytes(extraInputBytes)) : null;
					yield packet;
				}
			};
		}

		@Override
		public void handle(ClAbilityInputPacket payload, IPayloadContext context) {
			Player player = context.player();
			switch (payload.inputType) {
				case PRESS_CLICK -> {
					Ability ability = payload.abilityDecoded != null ? payload.abilityDecoded.getAbility(player) : null;
					if (AbilityInput.withConditionCheck(ability, player)) {
						AbilityInput.click(ability, player, payload.extraData, payload.timeTookToResolve);
					}
				}
				case PRESS_HOLD -> {
					Ability ability = payload.abilityDecoded != null ? payload.abilityDecoded.getAbility(player) : null;
					if (AbilityInput.withConditionCheck(ability, player)) {
						AbilityInput.startHolding(payload.key, ability, player, payload.extraData, payload.timeTookToResolve);
					}
				}
				case RELEASE -> AbilityInput.releaseHolding(payload.key, player);
			}
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}
	
}
