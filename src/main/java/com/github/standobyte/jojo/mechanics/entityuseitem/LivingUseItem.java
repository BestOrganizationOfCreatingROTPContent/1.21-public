package com.github.standobyte.jojo.mechanics.entityuseitem;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.util.mc.AttributeUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class LivingUseItem {
	
	static Player ___entity;
	static ItemCooldowns cooldowns;

	public static boolean serverSideRightClick(LivingEntity entity, @Nullable ServerPlayer actualPlayer, HitResult hitResult) {
		Level level = entity.level(); if (level.isClientSide()) return false;
		
		___entity = ServerPlayerLivingWrapper.create(entity, actualPlayer);
		cooldowns = actualPlayer.getCooldowns();
		if (actualPlayer != null) {
			actualPlayer.resetLastActionTime();
		}
		for (InteractionHand hand : InteractionHand.values()) {
			ItemStack item = entity.getItemInHand(hand);
			if (!item.isItemEnabled(level.enabledFeatures())) {
				continue;
			}

			InteractionResult result = null;
			if (hitResult != null) {
				result = _interactWithTarget(entity, actualPlayer, item, hand, hitResult);
			}

			if ((result == null || !result.consumesAction()) && !item.isEmpty()) {
				result = _useItemNoTarget(entity, item, hand);
			}

			if (result instanceof InteractionResult.Success success) {
				if (success.swingSource() == InteractionResult.SwingSource.SERVER) {
					entity.swing(hand, true);
				}
				return true;
			}
		}
		return false;
	}

	public static InteractionResult _interactWithTarget(LivingEntity entity, @Nullable ServerPlayer actualPlayer, 
			ItemStack item, InteractionHand hand, HitResult hitResult) {
		Level level = entity.level();
		ItemStack originalItemCopy = item.copy();

		return switch (hitResult.getType()) {
			case ENTITY -> {
				EntityHitResult entityHitResult = (EntityHitResult) hitResult;
				Entity targetEntity = entityHitResult.getEntity();
	
				if (!level.getWorldBorder().isWithinBounds(targetEntity.blockPosition())) {
					yield InteractionResult.FAIL;
				}
	
				if (!canInteractWithEntity(entity, targetEntity, 3.0)) {
					yield InteractionResult.FAIL;
				}
	
	
				Vec3 localHitPos = entityHitResult.getLocation().subtract(targetEntity.getX(), targetEntity.getY(), targetEntity.getZ());
				InteractionResult interactionResult = CommonHooks.onInteractEntityAt(___entity, targetEntity, localHitPos, hand);
				if (interactionResult == null) {
					interactionResult = targetEntity.interactAt(___entity, localHitPos, hand);
				}
	
				if (actualPlayer != null && interactionResult instanceof InteractionResult.Success success) {
					CriteriaTriggers.PLAYER_INTERACTED_WITH_ENTITY.trigger(actualPlayer, 
							success.wasItemInteraction() ? originalItemCopy : ItemStack.EMPTY, targetEntity);
				}
	
	
				if (!interactionResult.consumesAction()) {
					interactionResult = null;
					interactionResult = CommonHooks.onInteractEntity(___entity, targetEntity, hand);
					if (interactionResult == null) {
						interactionResult = targetEntity.interact(___entity, hand);
					}
	
					if (actualPlayer != null && interactionResult instanceof InteractionResult.Success success) {
						CriteriaTriggers.PLAYER_INTERACTED_WITH_ENTITY.trigger(actualPlayer, 
								success.wasItemInteraction() ? originalItemCopy : ItemStack.EMPTY, targetEntity);
					}
				}
	
				yield interactionResult;
			}
			case BLOCK -> {
				BlockHitResult blockHitResult = (BlockHitResult) hitResult;
				BlockPos blockPos = blockHitResult.getBlockPos();
				if (!level.getWorldBorder().isWithinBounds(blockPos) || !level.mayInteract(___entity, blockPos)) {
					yield InteractionResult.FAIL;
				}
	
				if (!canInteractWithBlock(entity, blockPos, 1.0)) {
					yield InteractionResult.FAIL;
				}
	
				Vec3 localHitPos = blockHitResult.getLocation().subtract(Vec3.atCenterOf(blockPos));
				double d0 = 1.0000001;
				if (Math.abs(localHitPos.x()) >= d0 || Math.abs(localHitPos.y()) >= d0 || Math.abs(localHitPos.z()) >= d0) {
					yield InteractionResult.FAIL;
				}
	
				int maxBuildHeight = entity.level().getMaxY();
				if (blockPos.getY() > maxBuildHeight) {
					if (actualPlayer != null) {
						actualPlayer.sendSystemMessage(Component.translatable("build.tooHigh", maxBuildHeight)
								.withStyle(ChatFormatting.RED), true);
					}
					yield InteractionResult.FAIL;
				}
	
	
				InteractionResult interactionResult = _useItemOnBlock(entity, actualPlayer, level, item, hand, blockHitResult);
				if (actualPlayer != null && interactionResult.consumesAction()) {
					CriteriaTriggers.ANY_BLOCK_USE.trigger(actualPlayer, blockHitResult.getBlockPos(), item.copy());
				}
	
				if (actualPlayer != null
						&& blockHitResult.getDirection() == Direction.UP
						&& !interactionResult.consumesAction()
						&& blockPos.getY() >= maxBuildHeight
						&& /* wasBlockPlacementAttempt */ !item.isEmpty() && (item.getItem() instanceof BlockItem || item.getItem() instanceof BucketItem) && !cooldowns.isOnCooldown(item)) {
					actualPlayer.sendSystemMessage(Component.translatable("build.tooHigh", maxBuildHeight)
							.withStyle(ChatFormatting.RED), true);
				}
	
				yield interactionResult; 
			}
			default -> InteractionResult.PASS;
		};
	}

	public static InteractionResult _useItemOnBlock(LivingEntity entity, @Nullable ServerPlayer actualPlayer, 
			Level level, ItemStack item, InteractionHand hand, BlockHitResult targetBlock) {
		BlockPos blockPos = targetBlock.getBlockPos();
		BlockState blockState = level.getBlockState(blockPos);
		if (!blockState.getBlock().isEnabled(level.enabledFeatures())) {
			return InteractionResult.FAIL;
		}
		PlayerInteractEvent.RightClickBlock event = CommonHooks.onRightClickBlock(___entity, hand, blockPos, targetBlock);
		if (event.isCanceled()) {
			return event.getCancellationResult();
		}
		
		UseOnContext context = new UseOnContext(___entity, hand, targetBlock);
		if (event.getUseItem() != TriState.FALSE) {
			InteractionResult result = item.onItemUseFirst(context);
			if (result != InteractionResult.PASS) return result;
		}
		boolean hasAnItem = !entity.getMainHandItem().isEmpty() || !entity.getOffhandItem().isEmpty();
		boolean shift = (___entity.isSecondaryUseActive() && hasAnItem) && !(
				entity.getMainHandItem().doesSneakBypassUse(level, blockPos, ___entity)
				&& entity.getOffhandItem().doesSneakBypassUse(level, blockPos, ___entity));
		ItemStack originalItemCopy = item.copy();
		if (event.getUseBlock().isTrue() || (event.getUseBlock().isDefault() && !shift)) {
			InteractionResult interactionResult = blockState.useItemOn(entity.getItemInHand(hand), level, ___entity, hand, targetBlock);
			if (interactionResult.consumesAction()) {
				if (actualPlayer != null) {
					CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(actualPlayer, blockPos, originalItemCopy);
				}
				return interactionResult;
			}

			if (interactionResult instanceof InteractionResult.TryEmptyHandInteraction && hand == InteractionHand.MAIN_HAND) {
				interactionResult = blockState.useWithoutItem(level, ___entity, targetBlock);
				if (interactionResult.consumesAction()) {
					if (actualPlayer != null) {
						CriteriaTriggers.DEFAULT_BLOCK_USE.trigger(actualPlayer, blockPos);
					}
					return interactionResult;
				}
			}
		}

		if (event.getUseItem().isTrue() || (!item.isEmpty() && !cooldowns.isOnCooldown(item))) {
			if (event.getUseItem().isFalse()) return InteractionResult.PASS;
			InteractionResult interactionResult = item.useOn(context);

			if (interactionResult.consumesAction() && actualPlayer != null) {
				CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(actualPlayer, blockPos, originalItemCopy);
			}

			return interactionResult;
		} else {
			return InteractionResult.PASS;
		}
	}

	public static InteractionResult _useItemNoTarget(LivingEntity entity, ItemStack item, InteractionHand hand) {
		if (cooldowns.isOnCooldown(item)) {
			return InteractionResult.PASS;
		} else {
			InteractionResult interactionResult = CommonHooks.onItemRightClick(___entity, hand);
			if (interactionResult != null) {
				return interactionResult;
			}

			Level level = entity.level();
			int itemCount = item.getCount();
			int damage = item.getDamageValue();
			interactionResult = item.use(level, ___entity, hand);

			ItemStack resultItem = null;
			if (interactionResult instanceof InteractionResult.Success success) {
				resultItem = success.heldItemTransformedTo();
			}
			if (resultItem == null) {
				resultItem = entity.getItemInHand(hand);
			}

			if ((
					resultItem == item
					&& resultItem.getCount() == itemCount
					&& resultItem.getUseDuration(entity) <= 0
					&& resultItem.getDamageValue() == damage)
					|| (
							interactionResult instanceof InteractionResult.Fail
							&& resultItem.getUseDuration(entity) > 0
							&& !entity.isUsingItem())) {
				return interactionResult;
			} else {
				if (item != resultItem) {
					entity.setItemInHand(hand, resultItem);
				}
				if (resultItem.isEmpty()) {
					entity.setItemInHand(hand, ItemStack.EMPTY);
				}

				return interactionResult;
			}
		}
	}


	public static boolean canInteractWithEntity(LivingEntity entity, Entity target, double distance) {
		if (target.isRemoved()) return false;
		double range = AttributeUtil.getValueOrDefault(entity, Attributes.ENTITY_INTERACTION_RANGE) + distance;
		return entity.getBoundingBox().distanceToSqr(target.getEyePosition()) < range * range;
	}

	public static boolean canInteractWithBlock(LivingEntity entity, BlockPos blockPos, double distance) {
		double range = AttributeUtil.getValueOrDefault(entity, Attributes.BLOCK_INTERACTION_RANGE) + distance;
		return new AABB(blockPos).distanceToSqr(entity.getEyePosition()) < range * range;
	}

}
