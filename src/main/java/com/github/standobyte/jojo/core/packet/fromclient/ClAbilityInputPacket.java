package com.github.standobyte.jojo.core.packet.fromclient;

import java.util.function.Function;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.PacketsRegister;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityInputHandler;
import com.github.standobyte.jojo.powersystem.ability.AbilityInputNetwork;
import com.github.standobyte.jojo.powersystem.ability.AbilityInputHandler.ClickInputType;
import com.mojang.datafixers.util.Either;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClAbilityInputPacket implements CustomPacketPayload {
	private final short key;
	private final ClickInputType inputType;
	private final PowerClass<?> powerClass;
	private final Either<Ability<?>, AbilityInputNetwork> ability;
	private final float timeTookToResolve;

	private Power<?> clUserPower;
	private RegistryFriendlyByteBuf extraData;
	
	public static ClAbilityInputPacket click(Power<?> power, Ability<?> ability, float timeTookToResolve) {
		return new ClAbilityInputPacket((short) 0, ClickInputType.PRESS_CLICK, power.getPowerClass(), power, Either.left(ability), timeTookToResolve);
	}
	
	public static ClAbilityInputPacket startHold(short key, Power<?> power, Ability<?> ability, float timeTookToResolve) {
		return new ClAbilityInputPacket(key, ClickInputType.PRESS_HOLD, power.getPowerClass(), power, Either.left(ability), timeTookToResolve);
	}
	
	public static ClAbilityInputPacket releaseHold(short key) {
		return new ClAbilityInputPacket(key, ClickInputType.RELEASE, null, null, null, 0);
	}
	
	private ClAbilityInputPacket(short key, ClickInputType inputType, PowerClass<?> powerClass, Power<?> userPower, 
			@Nullable Either<Ability<?>, AbilityInputNetwork> ability, float timeTookToResolve) {
		this.key = key;
		this.inputType = inputType;
		this.powerClass = powerClass;
		this.clUserPower = userPower;
		this.ability = ability;
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
			if (packet.inputType != ClickInputType.RELEASE) {
				PowerClass.NETWORK_CODEC.encode(buf, packet.powerClass);
				Ability<?> ability = packet.ability != null ? packet.ability.left().orElse(null) : null;
				AbilityInputNetwork.encodeInput(buf, ability, packet.clUserPower);
				if (packet.ability != null) {
					buf.writeFloat(packet.timeTookToResolve);
					ability.writeExtraInput(buf);
				}	
			}
		}

		@Override
		public ClAbilityInputPacket decode(RegistryFriendlyByteBuf buf) {
			short key = buf.readShort();
			ClickInputType inputType = buf.readEnum(ClickInputType.class);
			return switch (inputType) {
				case RELEASE -> ClAbilityInputPacket.releaseHold(key);
				default -> {
					PowerClass<?> powerClass = PowerClass.NETWORK_CODEC.decode(buf);
					AbilityInputNetwork ability = AbilityInputNetwork.decodeInput(buf);
					float timeTookToResolve = ability != null ? buf.readFloat() : 0;
					
					ClAbilityInputPacket packet = new ClAbilityInputPacket(key, inputType, powerClass, null, Either.right(ability), timeTookToResolve);
					// TODO WAIT A FUCKING SECOND - it if disconnects a player because it "found extra bytes", does this mean i can't do it like this anymore??
					packet.extraData = buf;
					yield packet;
				}
			};
		}

		@Override
		public void handle(ClAbilityInputPacket payload, IPayloadContext context) {
			Player player = context.player();
			switch (payload.inputType) {
				case PRESS_CLICK -> {
					Power<?> power = payload.powerClass.get(player);
					Ability<?> ability = payload.ability != null ? payload.ability.map(Function.identity(), id -> id.getAbility(power)) : null;
					AbilityInputHandler.click(ability, power, payload.extraData, payload.timeTookToResolve);
				}
				case PRESS_HOLD -> {
					Power<?> power = payload.powerClass.get(player);
					Ability<?> ability = payload.ability != null ? payload.ability.map(Function.identity(), id -> id.getAbility(power)) : null;
					AbilityInputHandler.startHolding(payload.key, ability, power, player, payload.extraData, payload.timeTookToResolve);
				}
				case RELEASE -> AbilityInputHandler.releaseHolding(payload.key, player);
			}
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}
	
}
