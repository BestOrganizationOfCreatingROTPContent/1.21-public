package com.github.standobyte.jojo.init.power;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.powersystem.standpower.effect.StandEffectType;

import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModStandEffects {
	public static final DeferredRegister<StandEffectType<?>> STAND_EFFECT_TYPES = DeferredRegister.create(JojoRegistries.STAND_EFFECTS_REG, JojoMod.MOD_ID);

}
