package com.github.standobyte.jojo.mixin.entitycontrol.mob.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.Mob;

@Mixin(Mob.class)
public interface MobInvoker {
	@Invoker("customServerAiStep") void callCustomServerAiStep();
}
