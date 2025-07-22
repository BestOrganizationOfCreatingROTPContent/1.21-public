package com.github.standobyte.jojo.mixin.standuseitems;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.github.standobyte.jojo.util.EntityWrapper;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;

@Mixin(Projectile.class)
public class ProjectileMixin {

	@ModifyVariable(method = "setOwner", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	public Entity jojo_ripples$setActualOwner(@Nullable Entity owner) {
		if (owner instanceof EntityWrapper wrapper) {
			return wrapper.getEntity();
		}
		return owner;
	}
}
