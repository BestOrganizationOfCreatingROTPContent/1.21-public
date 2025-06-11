package com.github.standobyte.jojo.util.target;

import java.util.Optional;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HitResultUtil {

	public static HitResult clipEntityLook(LivingEntity aiming, Predicate<Entity> entityFilter) {
		return HitResultUtil.clip(aiming.getEyePosition(), aiming.getLookAngle(), 
				aiming.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE), aiming.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE), 
				aiming.level(), entityFilter, aiming);
	}

	public static HitResult clip(Vec3 startingPos, Vec3 directionVec, double blockMaxRange, double entityMaxRange, 
			Level level, Predicate<Entity> entityFilter, @Nullable Entity aiming) {
		boolean hitFluids = false;
		CollisionContext entityCtx = aiming != null ? CollisionContext.of(aiming) : CollisionContext.empty();

		double maxRange = Math.max(blockMaxRange, entityMaxRange);


		// raytrace blocks

		Vec3 endPosBlocks = startingPos.add(directionVec.x * blockMaxRange, directionVec.y * blockMaxRange, directionVec.z * blockMaxRange);
		ClipContext blockClipCtx = new ClipContext(startingPos, endPosBlocks, 
				ClipContext.Block.OUTLINE, hitFluids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE, entityCtx);
		BlockHitResult blockHitResult = BlockGetter.traverseBlocks(blockClipCtx.getFrom(), blockClipCtx.getTo(), blockClipCtx, 
				(ClipContext ctx, BlockPos blockPos) -> {
					BlockState blockState = level.getBlockState(blockPos);
					FluidState fluidState = level.getFluidState(blockPos);
					Vec3 from = ctx.getFrom();
					Vec3 to = ctx.getTo();

					VoxelShape blockShape = ctx.getBlockShape(blockState, level, blockPos);
					BlockHitResult blockClip = blockShape.clip(from, to, blockPos);
					if (blockClip != null) {
						BlockHitResult blockI9nClip = blockState.getInteractionShape(level, blockPos).clip(from, to, blockPos);
						if (blockI9nClip != null
								&& blockI9nClip.getLocation().subtract(from).lengthSqr() < blockClip.getLocation().subtract(from).lengthSqr()) {
							blockClip = blockClip.withDirection(blockI9nClip.getDirection());
						}
					}

					VoxelShape fluidShape = ctx.getFluidShape(fluidState, level, blockPos);
					BlockHitResult fluidClip = fluidShape.clip(from, to, blockPos);

					double blockDist = blockClip == null ? Double.MAX_VALUE : ctx.getFrom().distanceToSqr(blockClip.getLocation());
					double fluidDist = fluidClip == null ? Double.MAX_VALUE : ctx.getFrom().distanceToSqr(fluidClip.getLocation());
					return blockDist <= fluidDist ? blockClip : fluidClip;
				}, 
				(ClipContext ctx) -> {
					Vec3 clipVec = ctx.getFrom().subtract(ctx.getTo());
					return BlockHitResult.miss(ctx.getTo(), Direction.getApproximateNearest(clipVec.x, clipVec.y, clipVec.z), BlockPos.containing(ctx.getTo()));
				});

		maxRange = entityMaxRange;
		AABB blockHitAABB = null;

		if (blockHitResult.getType() != HitResult.Type.MISS) {
			blockHitAABB = new AABB(blockHitResult.getBlockPos());
			Vec3 blockClipPos = blockHitAABB.clip(startingPos, endPosBlocks).orElse(blockHitResult.getLocation());
			maxRange = Math.min(maxRange, Math.sqrt(blockClipPos.distanceToSqr(startingPos)));
		}


		// raytrace entities

		Vec3 endPosEntities = startingPos.add(directionVec.x * maxRange, directionVec.y * maxRange, directionVec.z * maxRange);
		AABB boundingBox = new AABB(startingPos, endPosEntities).inflate(1.0, 1.0, 1.0);
		Predicate<Entity> filter = EntitySelector.CAN_BE_PICKED;

		double closestEntityDistSqr = maxRange * maxRange;
		Entity closestEntity = null;
		Vec3 closestEntityPos = null;
		AABB entityHitAABB = null;

		for (Entity potentialTarget : level.getEntities(aiming, boundingBox, filter)) {
			AABB targetAABB = potentialTarget.getBoundingBox().inflate(potentialTarget.getPickRadius());
			Optional<Vec3> targetClipPos = targetAABB.clip(startingPos, endPosEntities);
			if (targetAABB.contains(startingPos)) {
				if (closestEntityDistSqr >= 0.0) {
					closestEntity = potentialTarget;
					closestEntityPos = targetClipPos.orElse(startingPos);
					closestEntityDistSqr = 0.0;
					entityHitAABB = targetAABB;
				}
			} else if (targetClipPos.isPresent()) {
				Vec3 clipPos = targetClipPos.get();
				double distSqr = startingPos.distanceToSqr(clipPos);
				if (distSqr < closestEntityDistSqr || closestEntityDistSqr == 0.0) {
					if (aiming != null && potentialTarget.getRootVehicle() == aiming.getRootVehicle() && !potentialTarget.canRiderInteract()) {
						if (closestEntityDistSqr == 0.0) {
							closestEntity = potentialTarget;
							closestEntityPos = clipPos;
							entityHitAABB = targetAABB;
						}
					} else {
						closestEntity = potentialTarget;
						closestEntityPos = clipPos;
						closestEntityDistSqr = distSqr;
						entityHitAABB = targetAABB;
					}
				}
			}
		}

		EntityHitResult entityHitResult = closestEntity == null ? null : new EntityHitResult(closestEntity, closestEntityPos);


		HitResult hitResult;
		AABB hitResultAABB;
		if (entityHitResult != null) {
			hitResult = entityHitResult;
			maxRange = entityMaxRange;
			hitResultAABB = entityHitAABB;
		}
		else {
			hitResult = blockHitResult;
			maxRange = blockMaxRange;
			hitResultAABB = blockHitAABB;
		}

		// filter out if it's too far

		if (hitResult.getType() != HitResult.Type.MISS) {
			if (hitResultAABB.distanceToSqr(startingPos) > maxRange * maxRange) {
				Vec3 pos = hitResultAABB.getCenter();
				Direction direction = Direction.getApproximateNearest(pos.x - startingPos.x, pos.y - startingPos.y, pos.z - startingPos.z);
				hitResult = BlockHitResult.miss(pos, direction, BlockPos.containing(pos));
			}
		}

		return hitResult;
	}

}
