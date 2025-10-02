package com.github.standobyte.jojo.init.power;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.jojoimpl.stands.crazydiamond.DriedBloodDropsEffect;
import com.github.standobyte.jojo.powersystem.standpower.effect.StandEffectType;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModStandEffects {
	public static final DeferredRegister<StandEffectType<?>> STAND_EFFECT_TYPES = DeferredRegister.create(JojoRegistries.STAND_EFFECTS_REG, JojoMod.MOD_ID);


	public static final DeferredHolder<StandEffectType<?>, StandEffectType<DriedBloodDropsEffect>> CRAZY_D_BLOOD_DROPS = STAND_EFFECT_TYPES.register(
			"cd_blood_drops", key -> new StandEffectType<>(key, DriedBloodDropsEffect::new));
}
