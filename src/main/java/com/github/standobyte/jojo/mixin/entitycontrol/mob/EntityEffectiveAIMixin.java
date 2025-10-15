package com.github.standobyte.jojo.mixin.entitycontrol.mob;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.mechanics.entitycontrol.ServerEntityController;
import com.github.standobyte.jojo.mechanics.entitycontrol.client.ClientEntityController;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

@Mixin(Entity.class)
public class EntityEffectiveAIMixin {
	@Shadow private Level level;

	@Inject(method = "isEffectiveAi", at = @At("HEAD"), cancellable = true)
	public void jojo_ripples$clientSideEffectiveAIFlag(CallbackInfoReturnable<Boolean> ci) {
		if (level.isClientSide() && ClientEntityController.isBeingControlledByClient((Entity) (Object) this)) {
			ci.setReturnValue(true);
		}
	}

	@Inject(method = "isControlledByLocalInstance", at = @At("HEAD"), cancellable = true)
	public void jojo_ripples$sidedControlFlag(CallbackInfoReturnable<Boolean> ci) {
		Entity thisEntity = (Entity) (Object) this;
		if (level.isClientSide()) {
			if (ClientEntityController.isBeingControlledByClient(thisEntity)) {
				ci.setReturnValue(true);
			}
		}
		else {
			ServerEntityController controller = ServerEntityController.getCurrentController(thisEntity);
			if (controller != null && controller.suppressTargetEntity()) {
				ci.setReturnValue(false);
			}
		}
	}
}
