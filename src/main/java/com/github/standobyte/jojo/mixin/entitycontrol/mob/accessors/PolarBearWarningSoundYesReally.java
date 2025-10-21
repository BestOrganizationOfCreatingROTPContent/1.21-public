package com.github.standobyte.jojo.mixin.entitycontrol.mob.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.animal.PolarBear;

@Mixin(PolarBear.class)
public interface PolarBearWarningSoundYesReally {
	@Invoker("playWarningSound") void callPlayWarningSound();
}
