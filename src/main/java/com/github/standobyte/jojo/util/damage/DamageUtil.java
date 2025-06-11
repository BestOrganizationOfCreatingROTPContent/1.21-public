package com.github.standobyte.jojo.util.damage;

import com.github.standobyte.jojo.init.ModDamageTypes;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.scores.PlayerTeam;

public class DamageUtil {

//	public static boolean dealDamageAndSetOnFire(Entity entity, Predicate<Entity> hurtEntity, int fireSeconds, boolean stand) {
//		int fireTicks = entity.getRemainingFireTicks();
//		setOnFire(entity, fireSeconds, stand);
//		boolean dealtDamage = hurtEntity.test(entity);
//		if (!dealtDamage) {
//			entity.setRemainingFireTicks(fireTicks);
//		}
//		return dealtDamage;
//	}
//
//	public static void setOnFire(Entity entity, int fireSeconds, boolean stand) {
//		if (stand && entity instanceof StandEntity) {
//			((StandEntity) entity).setFireFromStand(fireSeconds);
//		}
//		else {
//			entity.setSecondsOnFire(fireSeconds);
//		}
//	}

	public static boolean hurtThroughInvulTicks(Entity target, DamageSource dmgSource, float dmgAmount) {
		if (target.level().isClientSide()) return false;
		
		int invulTime = target.invulnerableTime;
		target.invulnerableTime = 0;
		LivingEntity targetLiving = target instanceof LivingEntity ? (LivingEntity) target : null;
		float lastHurt = targetLiving != null ? targetLiving.lastHurt : 0;

//		if (!dmgSource.isBypassArmor() && dmgSource instanceof IModdedDamageSource && targetLiving != null) {
//			targetLiving.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(
//					cap -> cap.onHurtThroughInvul((IModdedDamageSource) dmgSource));
//		}
		boolean dealtDamage = target.hurtServer((ServerLevel) target.level(), dmgSource, dmgAmount);

		target.invulnerableTime = invulTime;
		if (targetLiving != null) {
			targetLiving.lastHurt = lastHurt;
		}
		return dealtDamage;
	}
	
	public static boolean canHurtStands(DamageSource dmgSource) {
		return dmgSource.is(ModDamageTypes.CAN_HURT_STANDS);
	}

	public static boolean isNotFriendlyFire(LivingEntity attacker, LivingEntity target) {
		if (attacker.is(target)) {
			return false;
		}
		if (!attacker.canAttack(target)) {
			return false;
		}

		PlayerTeam team1 = attacker.getTeam();
		PlayerTeam team2 = target.getTeam();
		if (team1 != null && team1.isAlliedTo(team2) && !team1.isAllowFriendlyFire()) {
			return false;
		}

		return true;
	}

//	public static DamageSource enderDragonDamageHack(DamageSource damageSource, Entity target) {
//		if (target instanceof EnderDragonEntity || target instanceof EnderDragonPartEntity) {
//			damageSource.setExplosion();
//		}
//		return damageSource;
//	}
//
//	public static float addArmorPiercing(float damage, float armorPiercing, @Nullable LivingEntity armoredTarget) {
//		if (armoredTarget != null && armorPiercing > 0) {
//			float armor = (float) armoredTarget.getArmorValue();
//			if (armor > 0) {
//				float toughness = (float) armoredTarget.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
//				armorPiercing = MathHelper.clamp(armorPiercing, 0, 1);
//				float damagePierced = MathHelper.lerp(armorPiercing, CombatRules.getDamageAfterAbsorb(damage, armor, toughness), damage);
//				damage = MathUtil.inverseArmorProtectionDamage(damagePierced, armor, toughness);
//			}
//		}
//		return damage;
//	}
//
//	public static void disableShield(PlayerEntity target, float chance) {
//		if (!target.level.isClientSide() && target.getRandom().nextFloat() < chance) {
//			target.getCooldowns().addCooldown(target.getUseItem().getItem(), 100);
//			target.stopUsingItem();
//			target.level.broadcastEntityEvent(target, (byte) 30);
//		}
//	}
//
//	public static boolean isMeleeAttack(DamageSource dmgSource) {
//		return getMeleeAttacker(dmgSource) != null;
//	}
//
//	@Nullable
//	public static LivingEntity getMeleeAttacker(DamageSource dmgSource) {
//		if (dmgSource.getEntity() != null && dmgSource.getDirectEntity() != null
//				&& dmgSource.getEntity().is(dmgSource.getDirectEntity()) && dmgSource.getEntity() instanceof LivingEntity) {
//			return (LivingEntity) dmgSource.getEntity();
//		}
//		return null;
//	}
//
//	public static void knockback(LivingEntity target, float strength, float yRotDeg) {
//		target.knockback(strength, 
//				(double) MathHelper.sin(yRotDeg * MathUtil.DEG_TO_RAD), 
//				(double) (-MathHelper.cos(yRotDeg * MathUtil.DEG_TO_RAD)));
//	}
//
//	public static void upwardsKnockback(LivingEntity target, float strength) {
//		strength *= (1.0F - (float) target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
//		if (strength != 0) {
//			target.setDeltaMovement(target.getDeltaMovement().add(0, strength, 0));
//		}
//
//		if (target instanceof StandEntity) {
//			LivingEntity standUser = ((StandEntity) target).getUser();
//			if (standUser != null && !standUser.is(target)) {
//				upwardsKnockback(standUser, strength);
//			}
//		}
//	}
//
//	public static void knockback3d(LivingEntity target, float strength, float xRot, float yRot) {
//		Vector3d knockbackVec = Vector3d.directionFromRotation(xRot, yRot);
//		LivingKnockBackEvent event = ForgeHooks.onLivingKnockBack(target, strength, knockbackVec.x, knockbackVec.z);
//		boolean addVertical = true;
//		if (event.isCanceled()) {
//			addVertical = target.getCapability(LivingUtilCapProvider.CAPABILITY).map(cap -> cap.didStackKnockbackInstead).orElse(false);
//		}
//		if (!addVertical) {
//			return;
//		}
//
//		strength = event.getStrength();
//		knockbackVec = new Vector3d(event.getRatioX(), knockbackVec.y, event.getRatioZ()).normalize();
//		strength *= (1.0F - (float) target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
//
//		if (strength != 0) {
//			target.setDeltaMovement(target.getDeltaMovement().add(knockbackVec.scale(strength)));
//		}
//
//		if (target instanceof StandEntity) {
//			LivingEntity standUser = ((StandEntity) target).getUser();
//			if (standUser != null && !standUser.is(target)) {
//				upwardsKnockback(standUser, (float) knockbackVec.y * strength);
//			}
//		}
//	}
//
//	public static void applyKnockbackStack(LivingEntity target, float pStrength, double pRatioX, double pRatioZ) {
//		pStrength *= 1 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
//		if (pStrength > 0) {
//			target.hasImpulse = true;
//			Vector3d speedCur = target.getDeltaMovement();
//			Vector3d knockback = (new Vector3d(pRatioX, 0.0D, pRatioZ)).normalize().scale(pStrength);
//			target.setDeltaMovement(
//					speedCur.x - knockback.x, 
//					Math.min(0.4D, speedCur.y + (double)pStrength), 
//					speedCur.z - knockback.z);
//		}
//
//		target.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(util -> {
//			util.didStackKnockbackInstead = true;
//		});
//	}
//
//	public static boolean isShieldBlockAngle(LivingEntity target, DamageSource damageSource) {
//		Vector3d damagePos = damageSource.getSourcePosition();
//		if (damagePos != null) {
//			Vector3d targetViewVec = target.getViewVector(1.0F);
//			Vector3d vecToTarget = damagePos.vectorTo(target.position());
//			vecToTarget = vecToTarget.normalize();
//			vecToTarget = new Vector3d(vecToTarget.x, 0, vecToTarget.z); // it's not normalized anymore though?
//			if (vecToTarget.dot(targetViewVec) < 0.0D) {
//				return true;
//			}
//		}
//
//		return false;
//	}
//
//	public static void suffocateTick(LivingEntity entity, float speed) {
//		if (entity.canBreatheUnderwater() || entity instanceof PlayerEntity && JojoModUtil.isUndeadOrVampiric((PlayerEntity) entity)
//				|| JojoModUtil.isDyingBody(entity) || entity instanceof IronGolemEntity) return;
//
//		if (entity.getAirSupply() > 0) {
//			Optional<HamonData> hamonOptional = INonStandPower.getNonStandPowerOptional(entity).resolve().flatMap(power -> power.getTypeSpecificData(ModPowers.HAMON.get()));
//			if (hamonOptional.isPresent()) {
//				HamonData hamon = hamonOptional.get();
//				speed /= 1 + hamonOptional.get().getBreathingLevel() * 0.04F;
//				hamon.suffocateTick(speed);
//			}
//
//			int airReduction = MathUtil.fractionRandomInc((double) entity.getMaxAirSupply() * MathHelper.clamp(speed, 0.0, 1.0)) + 4;
//			entity.setAirSupply(Math.max(entity.getAirSupply() - airReduction, -18));
//		}
//		else {
//			entity.hurt(SUFFOCATION, 1F);
//		}
//	}
//
//	public static float getDamageWithoutHeldItem(@Nullable LivingEntity entity) {
//		if (entity == null) {
//			return (float) Attributes.ATTACK_DAMAGE.getDefaultValue();
//		}
//		ItemStack heldItem = entity.getMainHandItem();
//		if (!heldItem.isEmpty()) {
//			Multimap<Attribute, AttributeModifier> itemModifiers = heldItem.getAttributeModifiers(EquipmentSlotType.MAINHAND);
//			if (itemModifiers.containsKey(Attributes.ATTACK_DAMAGE)) {
//				ModifiableAttributeInstance attackDamageAttribute = entity.getAttribute(Attributes.ATTACK_DAMAGE);
//				Collection<AttributeModifier> attackDamageModifiers = itemModifiers.get(Attributes.ATTACK_DAMAGE);
//
//				double damage = MCUtil.calcValueWithoutModifiers(attackDamageAttribute, 
//						attackDamageModifiers.stream().map(AttributeModifier::getId));
//				return (float) damage;
//			}
//		}
//		return (float) entity.getAttributeValue(Attributes.ATTACK_DAMAGE);
//	}
	
}
