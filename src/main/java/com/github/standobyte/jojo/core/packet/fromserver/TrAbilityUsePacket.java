package com.github.standobyte.jojo.core.packet.fromserver;

import java.util.function.Function;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientProxy;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class TrAbilityUsePacket implements CustomPacketPayload {
	private final int entityId;
	private final short key;
	private final ClickInputType inputType;
	private final PowerClass<?> powerClass;
	private final Either<Ability<?>, AbilityInputNetwork> ability;
	private final float timeTookToResolve;
	private RegistryFriendlyByteBuf extraData;
	
	public static TrAbilityUsePacket click(int entityId, PowerClass<?> powerClass, Ability<?> ability, float timeTookToResolve) {
		return new TrAbilityUsePacket(entityId, (short) 0, ClickInputType.PRESS_CLICK, powerClass, Either.left(ability), timeTookToResolve);
	}
	
	public static TrAbilityUsePacket startHold(int entityId, short key, PowerClass<?> powerClass, Ability<?> ability, float timeTookToResolve) {
		return new TrAbilityUsePacket(entityId, key, ClickInputType.PRESS_HOLD, powerClass, Either.left(ability), timeTookToResolve);
	}
	
	public static TrAbilityUsePacket releaseHold(int entityId, short key) {
		return new TrAbilityUsePacket(entityId, key, ClickInputType.RELEASE, null, null, 0);
	}
	
	private TrAbilityUsePacket(int entityId, short key, ClickInputType inputType, PowerClass<?> powerClass, @Nullable Either<Ability<?>, AbilityInputNetwork> ability, float timeTookToResolve) {
		this.entityId = entityId;
		this.key = key;
		this.inputType = inputType;
		this.powerClass = powerClass;
		this.ability = ability;
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
			if (packet.inputType != ClickInputType.RELEASE) {
				PowerClass.NETWORK_CODEC.encode(buf, packet.powerClass);
				Ability<?> ability = packet.ability != null ? packet.ability.left().orElse(null) : null;
				AbilityInputNetwork.encodeInput(buf, ability, null);
				if (packet.ability != null) {
					buf.writeFloat(packet.timeTookToResolve);
					ability.writeExtraInput(buf);
				}	
			}
		}

		@Override
		public TrAbilityUsePacket decode(RegistryFriendlyByteBuf buf) {
			int entityId = buf.readInt();
			short key = buf.readShort();
			ClickInputType inputType = buf.readEnum(ClickInputType.class);
			return switch (inputType) {
				case RELEASE -> TrAbilityUsePacket.releaseHold(entityId, key);
				default -> {
					PowerClass<?> powerClass = PowerClass.NETWORK_CODEC.decode(buf);
					AbilityInputNetwork ability = AbilityInputNetwork.decodeInput(buf);
					float timeTookToResolve = ability != null ? buf.readFloat() : 0;
					
					TrAbilityUsePacket packet = new TrAbilityUsePacket(entityId, key, inputType, powerClass, Either.right(ability), timeTookToResolve);
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
						Power<?> power = payload.powerClass.get(living);
						Ability<?> ability = payload.ability != null ? payload.ability.map(Function.identity(), id -> id.getAbility(power)) : null;
						AbilityInputHandler.click(ability, power, payload.extraData, payload.timeTookToResolve);
					}
					case PRESS_HOLD -> {
						Power<?> power = payload.powerClass.get(living);
						Ability<?> ability = payload.ability != null ? payload.ability.map(Function.identity(), id -> id.getAbility(power)) : null;
						AbilityInputHandler.startHolding(payload.key, ability, power, living, payload.extraData, payload.timeTookToResolve);
					}
					case RELEASE -> {
						AbilityInputHandler.releaseHolding(payload.key, living);
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
