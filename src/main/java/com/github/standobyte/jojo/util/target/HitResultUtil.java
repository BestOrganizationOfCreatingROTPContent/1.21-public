package com.github.standobyte.jojo.util.target;

import java.util.Optional;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
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

	public static HitResult clipEntityLook(LivingEntity aiming, Predicate<Entity> entityFilter, double standPrecision) {
		return HitResultUtil.clip(aiming.getEyePosition(), aiming.getLookAngle(), 
				aiming.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE), aiming.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE), 
				aiming.level(), entityFilter, aiming, standPrecision);
	}

	public static HitResult clip(Vec3 startingPos, Vec3 directionVec, double blockMaxRange, double entityMaxRange, 
			Level level, Predicate<Entity> entityFilter, @Nullable Entity aiming, double standPrecision) {
		boolean hitFluids = false;
		CollisionContext entityCtx = aiming != null ? CollisionContext.of(aiming) : CollisionContext.empty();

		double maxRange = Math.max(blockMaxRange, entityMaxRange);


		// raytrace blocks

		Vec3 endPosBlocks = startingPos.add(directionVec.x * blockMaxRange, directionVec.y * blockMaxRange, directionVec.z * blockMaxRange);
		ClipContext blockClipCtx = new ClipContext(startingPos, endPosBlocks, 
				ClipContext.Block.COLLIDER, hitFluids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE, entityCtx);
		BlockHitResult blockHitResult = clipBlocks(blockClipCtx, level);

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

		double closestEntityDistSqr = maxRange * maxRange;
		Entity closestEntity = null;
		Vec3 closestEntityPos = null;
		AABB entityHitAABB = null;

		for (Entity potentialTarget : level.getEntities(aiming, boundingBox, entityFilter)) {
			AABB targetAABB = potentialTarget.getBoundingBox().inflate(potentialTarget.getPickRadius());
			AABB precisionAABB = standPrecisionTargetHitbox(targetAABB, standPrecision);
			
			if (targetAABB.contains(startingPos)) {
				if (closestEntityDistSqr >= 0.0) {
					closestEntity = potentialTarget;
					Optional<Vec3> clip = targetAABB.clip(startingPos, endPosEntities);
					closestEntityPos = clip.orElse(startingPos);
					closestEntityDistSqr = 0.0;
					entityHitAABB = targetAABB;
				}
			}
			else {
				boolean hitWithPrecision = precisionAABB.contains(startingPos) || precisionAABB.clip(startingPos, endPosEntities).isPresent();
				if (hitWithPrecision) {
					Optional<Vec3> clip = targetAABB.clip(startingPos, targetAABB.getCenter());
					if (clip.isPresent()) {
						Vec3 clipPos = clip.get();
						double distSqr = startingPos.distanceToSqr(clipPos);
						if (distSqr < closestEntityDistSqr) {
							closestEntity = potentialTarget;
							closestEntityPos = clipPos;
							closestEntityDistSqr = distSqr;
							entityHitAABB = targetAABB;
						}
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
	
	public static AABB standPrecisionTargetHitbox(AABB aabb, double precision) {
		if (precision > 4) {
			double scale = precision / 5 + 0.2;

			double xSize = aabb.getXsize();
			double ySize = aabb.getYsize();
			double zSize = aabb.getZsize();

			double scaleX = Math.min(scale, 1 + 4 / (xSize * xSize));
			double scaleZ = Math.min(scale, 1 + 4 / (zSize * zSize));

			Vec3 center = aabb.getCenter();
			double inflX = xSize * scaleX / 2;
			double inflZ = zSize * scaleZ / 2;
			double inflY = (ySize + inflX - xSize / 2 + inflZ - zSize / 2) / 2;
			aabb = new AABB(
					center.x - inflX, center.y - inflY, center.z - inflZ,
					center.x + inflX, center.y + inflY, center.z + inflZ);
		}
		return aabb;
	}
	
	public static BlockHitResult clipBlocks(ClipContext blockClipCtx, Level level) {
		return BlockGetter.traverseBlocks(blockClipCtx.getFrom(), blockClipCtx.getTo(), blockClipCtx, 
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
	}

}
