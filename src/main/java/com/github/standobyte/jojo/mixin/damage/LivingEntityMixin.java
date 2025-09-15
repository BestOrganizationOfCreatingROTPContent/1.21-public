package com.github.standobyte.jojo.mixin.damage;

import java.util.Stack;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.util.damage.RipplesModifiedDamageSource;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.damagesource.DamageContainer;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
	public LivingEntityMixin(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	@Shadow protected LivingEntity lastHurtByMob;
	@Shadow protected Player lastHurtByPlayer;
	@Shadow public int lastHurtByPlayerTime;

	@Inject(method = "setLastHurtByMob", at = @At("TAIL"))
	public void jojo_ripples$entityResposibleForStandAttack(@Nullable LivingEntity attacker, CallbackInfo ci) {
		if (attacker instanceof StandEntity stand) {
			LivingEntity user = stand.getUser();
			if (user != null) {
				this.lastHurtByMob = user;
			}
		}
	}

//	@Inject(method = "resolvePlayerResponsibleForDamage", at = @At("TAIL"), cancellable = true)
//	public void jojo_ripples$playerResposibleForStandAttack(DamageSource damageSource, CallbackInfoReturnable<Player> ci) {
	@Inject(method = "hurt", at = @At(value = "INVOKE", target = "isDeadOrDying"))
	public void jojo_ripples$playerResposibleForStandAttack(DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> ci) {
//		if (ci.getReturnValue() == null) {
			if (damageSource.getEntity() instanceof StandEntity stand && stand.getUser() instanceof Player playerUser) {
				this.lastHurtByPlayerTime = 100;
				this.lastHurtByPlayer = playerUser;
			}
//		}
	}

	
	@Shadow(remap = false) Stack<DamageContainer> damageContainers;
	
	@Inject(method = "knockback", at = @At(
			value = "INVOKE", 
			target = "setDeltaMovement", 
			shift = At.Shift.AFTER))
	public void jojo_ripples$modifyKnockback(CallbackInfo ci) {
		if (!damageContainers.isEmpty()) {
			DamageContainer curDamage = damageContainers.peek();
			RipplesModifiedDamageSource.afterKnockbackApplied((LivingEntity) (Entity) this, curDamage);
		}
	}
	
}
