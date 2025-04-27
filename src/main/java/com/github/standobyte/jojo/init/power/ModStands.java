package com.github.standobyte.jojo.init.power;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.powersystem.Moveset;
import com.github.standobyte.jojo.powersystem.standpower.StandStats;
import com.github.standobyte.jojo.powersystem.standpower.entity.EntityStandType;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// TODO test the stand datapack configs
// XXX add a way to ban hardcoded stands
public class ModStands {
	public static final DeferredRegister<StandType> DEFAULT_STANDS = DeferredRegister.create(JojoRegistries.DEFAULT_STANDS_REG, JojoMod.MOD_ID);
	
	public static final DeferredHolder<StandType, EntityStandType> STAR_PLATINUM = DEFAULT_STANDS.register(
			"star_platinum", id -> 
			new EntityStandType(
					new StandStats.Builder()
					.power(18.5)
					.speed(19)
					.range(2, 10)
					.durability(20)
					.precision(20)
					.build(),

					// XXX addPowerTypeStandAbilities(Moveset.Builder<StandPower>)
					new Moveset.Builder()
					.addAbility(ModStandAbilities.PUNCH)
					.addAbility(ModStandAbilities.BARRAGE)
					.addAbility(ModStandAbilities.HEAVY_PUNCH)
					.addAbility(ModStandAbilities.HEAVY_CHARGED)
					.addAbility(ModStandAbilities.GRAB)
					.addAbility(ModStandAbilities.GRAB_TERRAIN)
					.addAbility(ModStandAbilities.GRABBED_RELEASE)
					.addAbility(ModStandAbilities.GRABBED_THROW)
					.addAbility(ModStandAbilities.BLOCK)
					.addAbility(ModStandAbilities.QUICKSTEP)
					.addAbility(ModStandAbilities.DODGE)
					.addAbility(ModStandAbilities.MANUAL_CONTROL)
					.addAbility(ModStandAbilities.SWAP_ITEMS)
					.addAbility(ModStandAbilities.ITEM_LMB)
					.addAbility(ModStandAbilities.ITEM_RMB)
					.addAbility(ModStandAbilities.STAND_LEAP)
					.addAbility(ModStandAbilities.SP_STAR_FINGER)
					.addAbility(ModStandAbilities.SP_STAR_FINGER_SWIPE)
					.addAbility(ModStandAbilities.SP_INHALE)
					.addAbility(ModStandAbilities.TIME_STOP), 

					id));
}
