package com.github.standobyte.jojo.core.packet.fromserver;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.client.sound.ClientsideSoundsHelper;
import com.github.standobyte.jojo.core.PacketsRegister;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PlayStandEntitySoundPacket(int entityId, Holder<SoundEvent> sound, boolean onlyForStandUsers, 
		float volume, float pitch) implements CustomPacketPayload {
	
	public PlayStandEntitySoundPacket(StandEntity standEntity, Holder<SoundEvent> sound, float volume, float pitch) {
		this(standEntity.getId(), sound, standEntity.onlyVisibleToStandUsers(), volume, pitch);
	}
	
	private static CustomPacketPayload.Type<PlayStandEntitySoundPacket> type;
	
	public static class Handler implements PacketsRegister.PacketCodecHandler<PlayStandEntitySoundPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<PlayStandEntitySoundPacket> type() {
			return type;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, PlayStandEntitySoundPacket> reader() {
			return STREAM_CODEC;
		}
		
		
		public static final StreamCodec<RegistryFriendlyByteBuf, PlayStandEntitySoundPacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.INT, PlayStandEntitySoundPacket::entityId,
				SoundEvent.STREAM_CODEC, PlayStandEntitySoundPacket::sound,
				ByteBufCodecs.BOOL, PlayStandEntitySoundPacket::onlyForStandUsers,
				ByteBufCodecs.FLOAT, PlayStandEntitySoundPacket::volume,
				ByteBufCodecs.FLOAT, PlayStandEntitySoundPacket::pitch,
				PlayStandEntitySoundPacket::new);

		@Override
		public void handle(PlayStandEntitySoundPacket payload, IPayloadContext context) {
			if (!payload.onlyForStandUsers || ClientGlobals.canHearStands) {
				SoundEvent sound = payload.sound.value();
				if (sound != null) {
					Entity entity = ClientProxy.getEntityById(payload.entityId);
					if (entity instanceof StandEntity stand) {
						ClientsideSoundsHelper.playEntityLingeringSound(stand, ClientsideSoundsHelper.withStandSkin(
								sound, stand.getStandId(), stand.getStandSkin()), 
								stand.getSoundSource(), payload.volume, payload.pitch, stand.level());
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
