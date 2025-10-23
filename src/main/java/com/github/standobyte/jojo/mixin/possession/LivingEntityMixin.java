package com.github.standobyte.jojo.mixin.possession;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.mechanics.possessionv2.LivingComponentPossession;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

	public LivingEntityMixin(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}
	
	@Inject(method = "isPickable", at = @At("HEAD"), cancellable = true)
	public void jojo_ripples$makePossessingNotPickable(CallbackInfoReturnable<Boolean> ci) {
		if (LivingComponentPossession.getEntityPossessedBy(this) != null) {
			ci.setReturnValue(false);
		}
	}
}
