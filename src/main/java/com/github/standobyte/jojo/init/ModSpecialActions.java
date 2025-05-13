package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.powersystem.entityaction.type.SpecialEntityActionType;

import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSpecialActions {
	public static final DeferredRegister<SpecialEntityActionType> ACTIONS = DeferredRegister.create(JojoRegistries.NON_POWER_ACTIONS_REG, JojoMod.MOD_ID);
	
}
