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
					.addAbility("punch", 				ModStandAbilities.PUNCH)
					.addAbility("barrage", 				ModStandAbilities.BARRAGE)
					.addAbility("heavy_punch", 			ModStandAbilities.HEAVY_PUNCH)
					.addAbility("heavy_charged", 		ModStandAbilities.HEAVY_CHARGED)
					.addAbility("grab", 				ModStandAbilities.GRAB)
					.addAbility("grab_terrain", 		ModStandAbilities.GRAB_TERRAIN)
					.addAbility("grabbed_release", 		ModStandAbilities.GRABBED_RELEASE)
					.addAbility("grabbed_throw", 		ModStandAbilities.GRABBED_THROW)
					.addAbility("block", 				ModStandAbilities.BLOCK)
					.addAbility("quickstep", 			ModStandAbilities.QUICKSTEP)
					.addAbility("dodge", 				ModStandAbilities.DODGE)
					.addAbility("manual_control",	 	ModStandAbilities.MANUAL_CONTROL)
					.addAbility("swap_items", 			ModStandAbilities.SWAP_ITEMS)
					.addAbility("item_lmb", 			ModStandAbilities.ITEM_LMB)
					.addAbility("item_rmb", 			ModStandAbilities.ITEM_RMB)
					.addAbility("leap", 				ModStandAbilities.STAND_LEAP)
					.addAbility("star_finger", 			ModStandAbilities.SP_STAR_FINGER)
					.addAbility("star_finger_swipe", 	ModStandAbilities.SP_STAR_FINGER_SWIPE)
					.addAbility("inhale", 				ModStandAbilities.SP_INHALE)
					.addAbility("time_stop", 			ModStandAbilities.TIME_STOP), 

					id));
}
