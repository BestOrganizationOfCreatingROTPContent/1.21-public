package com.github.standobyte.jojo.client.input;

import com.github.standobyte.jojo.core.packet.fromclient.ClAimTargetPacket;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.util.StandUtil;
import com.github.standobyte.jojo.util.mc.ActionTarget;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.PacketDistributor;

public class AimTarget {
	public static ActionTarget cameraEntityAimTarget = ActionTarget.EMPTY;
	public static ActionTarget cameraEntityAimTargetPrev = ActionTarget.EMPTY;
	public static ActionTarget playerAimTarget = ActionTarget.EMPTY;
	public static ActionTarget playerAimTargetPrev = ActionTarget.EMPTY;
	public static ActionTarget standAimTarget = ActionTarget.EMPTY;
	public static ActionTarget standAimTargetPrev = ActionTarget.EMPTY;
	
	public static void updateTarget(Minecraft mc, float partialTick) {
		cameraEntityAimTarget = mc.hitResult != null ? ActionTarget.fromVanilla(mc.hitResult) : ActionTarget.EMPTY;

		if (mc.level != null && mc.player != null) {
			if (mc.player == mc.cameraEntity || mc.cameraEntity == null) {
				playerAimTarget = cameraEntityAimTarget;
			}
			else {
				playerAimTarget = ActionTarget.EMPTY;
			}
			
			StandEntity stand = StandUtil.getSummonedStand(mc.player);
			if (stand != null) {
				standAimTarget = cameraEntityAimTarget;
				
				EntityActionInstance curAction = LivingComponentAction.getCurEntityAction(stand);
				if (curAction != null && curAction.standAimTarget != null) {
					standAimTarget = curAction.standAimTarget;
				}
			}
			else {
				standAimTarget = ActionTarget.EMPTY;
			}
		}
		else {
			playerAimTarget = ActionTarget.EMPTY;
			standAimTarget = ActionTarget.EMPTY;
		}
//		if (mc.player != null) {
//			StandEntity stand = StandUtil.getSummonedStand(mc.player);
//			if (stand != null) {
//				Entity cameraEntity = mc.cameraEntity;
//				if (cameraEntity == null) cameraEntity = mc.player;
//				
//			}
//		}
	}
	
	public static void updateTargetWithServer(Minecraft mc) {
		if (mc.level != null) {
			if (!playerAimTarget.equals(playerAimTargetPrev)) {
				PacketDistributor.sendToServer(new ClAimTargetPacket(playerAimTarget, ClAimTargetPacket.PacketType.PLAYER));
				playerAimTargetPrev = playerAimTarget;
			}
			
			if (!standAimTarget.equals(standAimTargetPrev)) {
				PacketDistributor.sendToServer(new ClAimTargetPacket(standAimTarget, ClAimTargetPacket.PacketType.STAND));
				standAimTargetPrev = standAimTarget;
			}
		}
	}
	
	
//	public static void pick(Minecraft mc, float partialTicks) {
//		Entity entity = mc.getCameraEntity();
//		if (entity != null) {
//			if (mc.level != null && mc.player != null) {
//				Profiler.get().push("pick");
//				double d0 = mc.player.blockInteractionRange();
//				double d1 = mc.player.entityInteractionRange();
//				HitResult hitresult = pick(entity, d0, d1, partialTicks);
//				mc.hitResult = hitresult;
//				mc.crosshairPickEntity = hitresult instanceof EntityHitResult entityhitresult ? entityhitresult.getEntity() : null;
//				Profiler.get().pop();
//			}
//		}
//	}
//
//	private static HitResult pick(Entity entity, double blockInteractionRange, double entityInteractionRange, float partialTick) {
//		double d0 = Math.max(blockInteractionRange, entityInteractionRange);
//		double d1 = Mth.square(d0);
//		Vec3 vec3 = entity.getEyePosition(partialTick);
//		HitResult hitresult = entity.pick(d0, partialTick, false);
//		double d2 = hitresult.getLocation().distanceToSqr(vec3);
//		if (hitresult.getType() != HitResult.Type.MISS) {
//			d1 = d2;
//			d0 = Math.sqrt(d2);
//		}
//
//		Vec3 vec31 = entity.getViewVector(partialTick);
//		Vec3 vec32 = vec3.add(vec31.x * d0, vec31.y * d0, vec31.z * d0);
//		float f = 1.0F;
//		AABB aabb = entity.getBoundingBox().expandTowards(vec31.scale(d0)).inflate(1.0, 1.0, 1.0);
//		EntityHitResult entityhitresult = ProjectileUtil.getEntityHitResult(entity, vec3, vec32, aabb, EntitySelector.CAN_BE_PICKED, d1);
//		return entityhitresult != null && entityhitresult.getLocation().distanceToSqr(vec3) < d2
//				? filterHitResult(entityhitresult, vec3, entityInteractionRange)
//				: filterHitResult(hitresult, vec3, blockInteractionRange);
//	}
//
//	private static HitResult filterHitResult(HitResult hitResult, Vec3 pos, double blockInteractionRange) {
//		Vec3 vec3 = hitResult.getLocation();
//		if (!vec3.closerThan(pos, blockInteractionRange)) {
//			Vec3 vec31 = hitResult.getLocation();
//			Direction direction = Direction.getApproximateNearest(vec31.x - pos.x, vec31.y - pos.y, vec31.z - pos.z);
//			return BlockHitResult.miss(vec31, direction, BlockPos.containing(vec31));
//		} else {
//			return hitResult;
//		}
//	}


}
