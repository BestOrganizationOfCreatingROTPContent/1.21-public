package com.github.standobyte.jojo.mixin.entitycontrol.mob;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.mechanics.entitycontrol.ServerEntityController;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

@Mixin(Mob.class)
public abstract class MobAIMixin extends LivingEntity {

	protected MobAIMixin(EntityType<? extends LivingEntity> entityType, Level level) {
		super(entityType, level);
	}
	
	@Redirect(method = "serverAiStep", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Mob;customServerAiStep()V"))
	public void jojo_ripples$cancelCustomAiOnClient(Mob thisAsMob) {
		if (!this.level().isClientSide()) {
			this.customServerAiStep();
		}
	}

	@Inject(method = "serverAiStep", at = @At("HEAD"), cancellable = true)
	public void jojo_ripples$manualMobControl(CallbackInfo ci) {
		ServerEntityController controller = ServerEntityController.getCurrentController(this);
		if (controller != null && controller.suppressTargetEntity()) {
			this.customServerAiStep();
			ci.cancel();
		}
	}
	
	@Shadow protected abstract void customServerAiStep();
	
	
	// FIXME cancel Mob#customServerAiStep() call on client side
}
