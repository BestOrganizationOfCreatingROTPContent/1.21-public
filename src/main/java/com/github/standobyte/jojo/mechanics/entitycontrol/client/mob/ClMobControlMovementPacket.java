package com.github.standobyte.jojo.mechanics.entitycontrol.client.mob;

import com.github.standobyte.jojo.core.PacketsRegister;
import com.github.standobyte.jojo.mechanics.entitycontrol.ServerEntityController;
import com.google.common.primitives.Floats;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClMobControlMovementPacket(int entityId, double x, double y, double z, float xRot, float yRot) implements CustomPacketPayload {
	private static CustomPacketPayload.Type<ClMobControlMovementPacket> type;

	public static class Handler implements PacketsRegister.PacketCodecHandler<ClMobControlMovementPacket> {

		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<ClMobControlMovementPacket> type() {
			return type;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, ClMobControlMovementPacket> reader() {
			return STREAM_CODEC;
		}


		public static final StreamCodec<RegistryFriendlyByteBuf, ClMobControlMovementPacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.INT, ClMobControlMovementPacket::entityId,
				ByteBufCodecs.DOUBLE, ClMobControlMovementPacket::x,
				ByteBufCodecs.DOUBLE, ClMobControlMovementPacket::y,
				ByteBufCodecs.DOUBLE, ClMobControlMovementPacket::z,
				ByteBufCodecs.FLOAT, ClMobControlMovementPacket::xRot,
				ByteBufCodecs.FLOAT, ClMobControlMovementPacket::yRot,
				ClMobControlMovementPacket::new);

		@Override
		public void handle(ClMobControlMovementPacket packet, IPayloadContext context) {
			ServerPlayer player = (ServerPlayer) context.player();
			if (isInvalid(packet)) {
				player.connection.disconnect(Component.translatable("multiplayer.disconnect.invalid_vehicle_movement"));
			}
			
			Entity curControlTarget = ServerEntityController.getControlTarget(player);
			if (curControlTarget != null && curControlTarget.getId() == packet.entityId) {
				manualControlPacket(curControlTarget, packet);
			}
		}
		
		public void manualControlPacket(Entity target, ClMobControlMovementPacket msg) {
			double posX1 = target.getX(); // d0
			double posY1 = target.getY(); // d1
			double posZ1 = target.getZ(); // d2
			double posXcl = msg.x(); // d3
			double posYcl = msg.y(); // d4
			double posZcl = msg.z(); // d5
			float xRot = msg.xRot();
			float yRot = msg.yRot();
			double diffX = posXcl - posX1; // d6
			double diffY = posYcl - posY1; // d7
			double diffZ = posZcl - posZ1; // d8
			target.move(MoverType.PLAYER, new Vec3(diffX, diffY, diffZ));
			target.absMoveTo(posXcl, posYcl, posZcl, yRot, xRot);
		}
		
//		public void manualControlPacket2(Entity target, ClMobControlMovementPacket msg) {
//			double d0 = entity.getX();
//			double d1 = entity.getY();
//			double d2 = entity.getZ();
//			double d3 = clampHorizontal(packet.getX());
//			double d4 = clampVertical(packet.getY());
//			double d5 = clampHorizontal(packet.getZ());
//			float f = Mth.wrapDegrees(packet.getYRot());
//			float f1 = Mth.wrapDegrees(packet.getXRot());
//			double d6 = d3 - this.vehicleFirstGoodX;
//			double d7 = d4 - this.vehicleFirstGoodY;
//			double d8 = d5 - this.vehicleFirstGoodZ;
//			double d9 = entity.getDeltaMovement().lengthSqr();
//			double d10 = d6 * d6 + d7 * d7 + d8 * d8;
//			if (d10 - d9 > 100.0 && !this.isSingleplayerOwner()) {
//				LOGGER.warn("{} (vehicle of {}) moved too quickly! {},{},{}", entity.getName().getString(), this.player.getName().getString(), d6, d7, d8);
//				this.send(new ClientboundMoveVehiclePacket(entity));
//				return;
//			}
//
//			boolean flag = serverlevel.noCollision(entity, entity.getBoundingBox().deflate(0.0625));
//			d6 = d3 - this.vehicleLastGoodX;
//			d7 = d4 - this.vehicleLastGoodY - 1.0E-6;
//			d8 = d5 - this.vehicleLastGoodZ;
//			boolean flag1 = entity.verticalCollisionBelow;
//			if (entity instanceof LivingEntity livingentity && livingentity.onClimbable()) {
//				livingentity.resetFallDistance();
//			}
//
//			entity.move(MoverType.PLAYER, new Vec3(d6, d7, d8));
//			double d11 = d7;
//			d6 = d3 - entity.getX();
//			d7 = d4 - entity.getY();
//			if (d7 > -0.5 || d7 < 0.5) {
//				d7 = 0.0;
//			}
//
//			d8 = d5 - entity.getZ();
//			d10 = d6 * d6 + d7 * d7 + d8 * d8;
//			boolean flag2 = false;
//			if (d10 > 0.0625) {
//				flag2 = true;
//				LOGGER.warn("{} (vehicle of {}) moved wrongly! {}", entity.getName().getString(), this.player.getName().getString(), Math.sqrt(d10));
//			}
//
//			entity.absMoveTo(d3, d4, d5, f, f1);
//			resyncPlayerWithVehicle(entity); // Neo - Resync player position on vehicle moving
//			boolean flag3 = serverlevel.noCollision(entity, entity.getBoundingBox().deflate(0.0625));
//			if (flag && (flag2 || !flag3)) {
//				entity.absMoveTo(d0, d1, d2, f, f1);
//				resyncPlayerWithVehicle(entity); // Neo - Resync player position on vehicle moving
//				this.send(new ClientboundMoveVehiclePacket(entity));
//				return;
//			}
//
//			this.player.serverLevel().getChunkSource().move(this.player);
//			Vec3 vec3 = new Vec3(entity.getX() - d0, entity.getY() - d1, entity.getZ() - d2);
//			this.player.setKnownMovement(vec3);
//			this.player.checkMovementStatistics(vec3.x, vec3.y, vec3.z);
//			this.player.checkRidingStatistics(vec3.x, vec3.y, vec3.z); // Neo: check riding stats too as vanilla checks them in rideTick based on the assumption that Entity#rideTick will move the entity, which we break
//			this.clientVehicleIsFloating = d11 >= -0.03125
//					&& !flag1
//					&& !this.server.isFlightAllowed()
//					&& !entity.isNoGravity()
//					&& this.noBlocksAround(entity);
//			this.vehicleLastGoodX = entity.getX();
//			this.vehicleLastGoodY = entity.getY();
//			this.vehicleLastGoodZ = entity.getZ();
//		}

		private boolean isInvalid(ClMobControlMovementPacket msg) {
			return Double.isNaN(msg.x) || Double.isNaN(msg.y) || Double.isNaN(msg.z)
					|| !Floats.isFinite(msg.xRot) || !Floats.isFinite(msg.yRot);
		}

	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
