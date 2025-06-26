package com.github.standobyte.jojo.core;

import com.github.standobyte.jojo.core.packet.fromclient.ClAbilityInputPacket;
import com.github.standobyte.jojo.core.packet.fromclient.ClAimTargetPacket;
import com.github.standobyte.jojo.core.packet.fromclient.ClNoParamsPacket;
import com.github.standobyte.jojo.core.packet.fromclient.ClSetStandSkinPacket;
import com.github.standobyte.jojo.core.packet.fromserver.DatapackStandsPacket;
import com.github.standobyte.jojo.core.packet.fromserver.StandEntitySoundPacket;
import com.github.standobyte.jojo.core.packet.fromserver.TrAbilityUsePacket;
import com.github.standobyte.jojo.core.packet.fromserver.TrAimTargetPacket;
import com.github.standobyte.jojo.core.packet.fromserver.TrPowerStandInstancePacket;
import com.github.standobyte.jojo.core.packet.fromserver.TrPowerTypePacket;
import com.github.standobyte.jojo.core.packet.fromserver.TrSetStandEntityPacket;
import com.github.standobyte.jojo.core.packet.fromserver.TrStandSkinPacket;
import com.github.standobyte.jojo.mechanics.clothes.TrClothesItemsPacket;
import com.github.standobyte.jojo.mechanics.grab.TrSetGrabbedEntityPacket;
import com.github.standobyte.jojo.powersystem.entityaction.netcode.TrEntityActionInstancePacket;
import com.github.standobyte.jojo.powersystem.entityaction.netcode.TrEntityActionPhaseTimePacket;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class PacketsRegister {

	public static void register(RegisterPayloadHandlersEvent event) {
		PayloadRegistrar registrar = event.registrar("1");
		registerPacket(registrar, PayloadRegistrar::playToServer, new ClAbilityInputPacket.Handler(JojoMod.resLoc("clkey")));
		registerPacket(registrar, PayloadRegistrar::playToServer, new ClNoParamsPacket.Handler(JojoMod.resLoc("clsignal")));
		registerPacket(registrar, PayloadRegistrar::playToServer, new ClAimTargetPacket.Handler(JojoMod.resLoc("clientaim")));
		registerPacket(registrar, PayloadRegistrar::playToServer, new ClSetStandSkinPacket.Handler(JojoMod.resLoc("clskin")));

		registerPacket(registrar, PayloadRegistrar::playToClient, new DatapackStandsPacket.Handler(JojoMod.resLoc("datastands")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new TrAbilityUsePacket.Handler(JojoMod.resLoc("abilityuse")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new TrEntityActionInstancePacket.Handler(JojoMod.resLoc("action")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new TrEntityActionPhaseTimePacket.Handler(JojoMod.resLoc("actionphase")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new TrPowerStandInstancePacket.Handler(JojoMod.resLoc("standinst")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new TrPowerTypePacket.Handler(JojoMod.resLoc("plpowertype")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new TrSetStandEntityPacket.Handler(JojoMod.resLoc("standentity")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new TrAimTargetPacket.Handler(JojoMod.resLoc("aim")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new TrStandSkinPacket.Handler(JojoMod.resLoc("standskin")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new StandEntitySoundPacket.Handler(JojoMod.resLoc("standsound")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new TrClothesItemsPacket.Handler(JojoMod.resLoc("clothes")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new TrSetGrabbedEntityPacket.Handler(JojoMod.resLoc("grab")));
	}

	
	public static interface PacketHandler<T extends CustomPacketPayload> {
		CustomPacketPayload.Type<T> type();
		void handle(T payload, IPayloadContext context);
	}
	
	public static interface PacketOGHandler<T extends CustomPacketPayload> extends PacketHandler<T> {
		void encode(T packet, RegistryFriendlyByteBuf buf);
		T decode(RegistryFriendlyByteBuf buf);
	}
	
	public static interface PacketCodecHandler<T extends CustomPacketPayload> extends PacketHandler<T> {
		StreamCodec<? super RegistryFriendlyByteBuf, T> reader();
	}
	
	private static <T extends CustomPacketPayload> void registerPacket(PayloadRegistrar registrar, PacketType packetType, PacketOGHandler<T> handler) {
		packetType.register(registrar, handler.type(), StreamCodec.ofMember(handler::encode, handler::decode), handler::handle);
	}
	
	private static <T extends CustomPacketPayload> void registerPacket(PayloadRegistrar registrar, PacketType packetType, PacketCodecHandler<T> handler) {
		packetType.register(registrar, handler.type(), handler.reader(), handler::handle);
	}
	
	@FunctionalInterface
	private static interface PacketType {
		<T extends CustomPacketPayload> void register(PayloadRegistrar registrar, 
				CustomPacketPayload.Type<T> type, 
				StreamCodec<? super RegistryFriendlyByteBuf, T> reader, 
				IPayloadHandler<T> handler);
	}
}
