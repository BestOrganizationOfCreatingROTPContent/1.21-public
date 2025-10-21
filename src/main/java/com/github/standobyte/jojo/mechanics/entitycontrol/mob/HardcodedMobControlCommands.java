package com.github.standobyte.jojo.mechanics.entitycontrol.mob;

import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.mixin.entitycontrol.mob.accessors.MeleeAttackGoalInvoker;
import com.github.standobyte.jojo.mixin.entitycontrol.mob.accessors.MobInvoker;
import com.github.standobyte.jojo.mixin.entitycontrol.mob.accessors.SkeletonAccessor;
import com.github.standobyte.jojo.util.mc.EntityEvents;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class HardcodedMobControlCommands {
	
	public enum CommandType {
		PRESS_LMB,
		RELEASE_LMB,
		PRESS_RMB,
		RELEASE_RMB,
		HOLDING_RMB,
		
		EMPTY_MAIN_HAND,
		SWAP_ITEMS,
		TOSS,
		PICK_SLOT,
		WITCH_PICK_DRINK_POTION,
		WITCH_PICK_SPLASH_POTION,
	}
	
	
	public static void serverTickControlledMob(Mob mob) {
		switch (mob) {
			case AbstractPiglin piglin -> {
				// conversion to zombified version
				// which DOESN'T MAKE FUCKING SENSE to be a part of mob AI
				((MobInvoker) piglin).callCustomServerAiStep();
			}
			case Hoglin hoglin -> {
				// same
				((MobInvoker) hoglin).callCustomServerAiStep();
			}
			default -> {}
		}
		Set<WrappedGoal> availableAIGoals = mob.goalSelector.getAvailableGoals();
		for (WrappedGoal wrappedGoal : availableAIGoals) {
			Goal goal = wrappedGoal.getGoal();
			if (goal instanceof MeleeAttackGoal meleeAttack) {
				MeleeAttackGoalInvoker despiteAllMyRage = (MeleeAttackGoalInvoker) meleeAttack;
				int cooldown = despiteAllMyRage.callGetTicksUntilNextAttack();
				if (cooldown > 0) { despiteAllMyRage.setTicksUntilNextAttack(cooldown - 1); }
			}
		}
		
	}
	
	public static void onHotbarPacket(Mob mob, CommandType commandType, int slot, HitResult target) {
		switch (commandType) {
			case PRESS_LMB -> {
				Set<WrappedGoal> availableAIGoals = mob.goalSelector.getAvailableGoals();
				for (WrappedGoal wrappedGoal : availableAIGoals) {
					Goal goal = wrappedGoal.getGoal();
					if (goal instanceof MeleeAttackGoal meleeAttack) {
						Entity targetEntity = target.getType() == HitResult.Type.ENTITY ? ((EntityHitResult) target).getEntity() : null;
						LivingEntity targetLiving = targetEntity instanceof LivingEntity __ ? __ : null;
						MeleeAttackGoalInvoker despiteAllMyRage = (MeleeAttackGoalInvoker) meleeAttack;
						boolean canAttack = targetLiving != null && despiteAllMyRage.callCanPerformAttack(targetLiving);
						int cooldown = despiteAllMyRage.callGetTicksUntilNextAttack();
						if (canAttack) {
							// yes, it will do the check twice, but fox and polar bear override this method completely for some dumbass fucking reason
							// god i hate this game's source code
							despiteAllMyRage.callCheckAndPerformAttack(targetLiving);
						}
						else if (cooldown <= 0) {
							switch (mob) {
								case IronGolem ironGolem -> { mob.makeSound(SoundEvents.IRON_GOLEM_ATTACK); }
								case Hoglin hoglin -> { mob.makeSound(SoundEvents.HOGLIN_ATTACK); }
								case Ravager ravager -> { mob.makeSound(SoundEvents.RAVAGER_ATTACK); }
//								case Warden warden -> {}
								case Zoglin zoglin -> { mob.makeSound(SoundEvents.ZOGLIN_ATTACK); }
								default -> {}
							}
							switch (mob) {
								case PolarBear polarBear -> {
									if (targetLiving != null && mob.distanceToSqr(targetLiving) < (double)((targetLiving.getBbWidth() + 3.0F) * (targetLiving.getBbWidth() + 3.0F))) {
										if (despiteAllMyRage.callIsTimeToAttack()) {
											polarBear.setStanding(false);
										}
										if (cooldown <= 10) {
											polarBear.setStanding(true);
										}
									} else {
										polarBear.setStanding(false);
									}
								}
								case Fox fox -> {
								}
								default -> {
									mob.swing(InteractionHand.MAIN_HAND);
								}
							}
							
							despiteAllMyRage.callResetAttackCooldown();
							mob.level().broadcastEntityEvent(mob, EntityEvents.MOB_ATTACK_ANIMATION);
						}
					}
				}
			}
			case RELEASE_LMB -> {
				
			}
			case PRESS_RMB -> {
				for (InteractionHand hand : InteractionHand.values()) {
					ItemStack item = mob.getItemInHand(hand);
					if (!item.isEmpty()) {
						mob.startUsingItem(hand);
						if (mob.isUsingItem()) {
							break;
						}
					}
				}
				switch (mob) {
					case AbstractSkeleton skeleton -> {
						if (mob.isUsingItem()) {
							skeleton.setAggressive(true);
						}
					}
					default -> {}
				}
			}
			case HOLDING_RMB -> {
				
			}
			case RELEASE_RMB -> {
				switch (mob) {
					case AbstractSkeleton skeleton -> {
						// copypasted AbstractSkeleton#performRangedAttack(LivingEntity target, float velocity), but using the look vector
						// instead of a specific LivingEntity target because there is none
						for (InteractionHand hand : InteractionHand.values()) {
							ItemStack weapon = skeleton.getItemInHand(hand);
							if (!weapon.isEmpty() && weapon.getItem() instanceof BowItem) {
								int ticksBowUsed = skeleton.getTicksUsingItem();
								if (ticksBowUsed >= 20) {
									float velocity = BowItem.getPowerForTime(ticksBowUsed);
									ItemStack arrowItem = skeleton.getProjectile(weapon);
									AbstractArrow arrowEntity = ((SkeletonAccessor) skeleton).callGetArrow(arrowItem, velocity, weapon);
									if (weapon.getItem() instanceof ProjectileWeaponItem weaponItem) {
										arrowEntity = weaponItem.customArrow(arrowEntity, arrowItem, weapon);
									}
									Vec3 lookVec = skeleton.getLookAngle();
									double x = lookVec.x;
									double y = lookVec.y;
									double z = lookVec.z;
									arrowEntity.shoot(x, y, z, 1.6F, (float)(14 - skeleton.level().getDifficulty().getId() * 4));
									skeleton.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (skeleton.getRandom().nextFloat() * 0.4F + 0.8F));
									skeleton.level().addFreshEntity(arrowEntity);
								}

								break;
							}
						}

						mob.stopUsingItem();
						skeleton.setAggressive(false);
					}
					default -> {
						if (mob.isUsingItem()) {
							mob.stopUsingItem();
						}
					}
				}
			}
			
			/* VERY careful with this one, this MUST NOT remove any player-made items that the mob might have picked up
			 * (either on its own or when controlled by a player) to not potentially enable griefing
			 */
			case EMPTY_MAIN_HAND -> {
				if (mob instanceof Witch) {
					clearHeldItem(mob, heldItem -> {
						Item item = heldItem.getItem();
						return item == Items.POTION || item == Items.SPLASH_POTION;
					});
				}
			}
			case SWAP_ITEMS -> {
				
			}
			case TOSS -> {
				
			}
			case PICK_SLOT -> {
				
			}
			case WITCH_PICK_DRINK_POTION ->  {
				if (mob instanceof Witch) {
					if (slot >= 0 && slot < WITCH_DRINK_POTIONS.length) {
						mob.setItemInHand(InteractionHand.MAIN_HAND, WITCH_DRINK_POTIONS[slot].copy());
					}
					else {
						clearHeldItem(mob, heldItem -> {
							Item item = heldItem.getItem();
							return item == Items.POTION || item == Items.SPLASH_POTION;
						});
					}
				}
			}
			case WITCH_PICK_SPLASH_POTION ->  {
				if (mob instanceof Witch) {
					if (slot >= 0 && slot < WITCH_SPLASH_POTIONS.length) {
						mob.setItemInHand(InteractionHand.MAIN_HAND, WITCH_SPLASH_POTIONS[slot].copy());
					}
					else {
						clearHeldItem(mob, heldItem -> {
							Item item = heldItem.getItem();
							return item == Items.POTION || item == Items.SPLASH_POTION;
						});
					}
				}
			}
		}
	}
	
	protected static void clearHeldItem(LivingEntity entity, Predicate<ItemStack> condition) {
		ItemStack heldItem = entity.getMainHandItem();
		if (!heldItem.isEmpty() && condition.test(heldItem)) {
			entity.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		}
	}


	public static enum WitchPotionMode { DRINK, SPLASH }
	public static ItemStack[] WITCH_DRINK_POTIONS = new ItemStack[] {
			PotionContents.createItemStack(Items.POTION, Potions.HEALING),
			PotionContents.createItemStack(Items.POTION, Potions.FIRE_RESISTANCE),
			PotionContents.createItemStack(Items.POTION, Potions.SWIFTNESS),
			PotionContents.createItemStack(Items.POTION, Potions.WATER_BREATHING),
	};
	public static ItemStack[] WITCH_SPLASH_POTIONS = new ItemStack[] {
			PotionContents.createItemStack(Items.SPLASH_POTION, Potions.HARMING),
			PotionContents.createItemStack(Items.SPLASH_POTION, Potions.HEALING),
			PotionContents.createItemStack(Items.SPLASH_POTION, Potions.REGENERATION),
			PotionContents.createItemStack(Items.SPLASH_POTION, Potions.SLOWNESS),
			PotionContents.createItemStack(Items.SPLASH_POTION, Potions.POISON),
			PotionContents.createItemStack(Items.SPLASH_POTION, Potions.WEAKNESS),
	};
	@Nullable
	public static ItemStack[] getWitchPotions(@Nullable WitchPotionMode mode) {
		if (mode == null) return null;
		return switch (mode) {
			case SPLASH -> WITCH_SPLASH_POTIONS;
			case DRINK -> WITCH_DRINK_POTIONS;
		};
	}
	
}
