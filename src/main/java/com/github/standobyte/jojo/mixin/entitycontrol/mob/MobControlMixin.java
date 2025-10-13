package com.github.standobyte.jojo.mixin.entitycontrol.mob;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.mechanics.entitycontrol.ServerEntityController;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

@Mixin(Mob.class)
public abstract class MobControlMixin extends LivingEntity {

	protected MobControlMixin(EntityType<? extends LivingEntity> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(method = "serverAiStep", at = @At("HEAD"), cancellable = true)
	public void jojo_ripples$manualMobControl(CallbackInfo ci) {
		if (ServerEntityController.getControllerEntity(this) != null) {
			ci.cancel();
		}
	}
}
