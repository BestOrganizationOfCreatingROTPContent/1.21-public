package com.github.standobyte.jojo.init.power;

import java.util.Collections;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.powersystem.MovesetBuilder;
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
					new MovesetBuilder()
					.addAbility("punch", 				ModStandAbilities.PUNCH, true, 
							punch -> Collections.addAll(punch.punchNames, "punch1", "punch2", "punch3", "punch4", "low_kick")
					)
					.addAbility("punch1", 				ModStandAbilities.PUNCH, false)
					.addAbility("punch2", 				ModStandAbilities.PUNCH, false)
					.addAbility("punch3", 				ModStandAbilities.PUNCH, false)
					.addAbility("punch4", 				ModStandAbilities.PUNCH, false)
					.addAbility("low_kick", 			ModStandAbilities.PUNCH, false)

					.addAbility("heavy_punch", 			ModStandAbilities.HEAVY_PUNCH, true)
					.addAbility("heavy_punch1", 		ModStandAbilities.HEAVY_PUNCH, false)
					.addAbility("heavy_punch2", 		ModStandAbilities.HEAVY_PUNCH, false)
					
					.addAbility("uppercut", 			ModStandAbilities.HEAVY_PUNCH, false)
					.addAbility("ground_throw", 		ModStandAbilities.HEAVY_PUNCH, false)
					.addAbility("heavy_kick", 			ModStandAbilities.HEAVY_PUNCH, false)
					
					.addAbility("barrage", 				ModStandAbilities.BARRAGE, true)
					.addAbility("kick_barrage", 		ModStandAbilities.BARRAGE, false)
					
					.addAbility("grab", 				ModStandAbilities.GRAB, true)
					.addAbility("grab_punch1", 			ModStandAbilities.PUNCH, false)
					.addAbility("grab_punch2", 			ModStandAbilities.PUNCH, false)
					.addAbility("grab_punch3", 			ModStandAbilities.PUNCH, false)
					.addAbility("grab_punch4", 			ModStandAbilities.PUNCH, false)
					.addAbility("grab_ground_slam", 	ModStandAbilities.HEAVY_PUNCH, false)
					.addAbility("grab_barrage", 		ModStandAbilities.BARRAGE, false)
					.addAbility("grabbed_throw", 		ModStandAbilities.GRAB_THROW, false)
					
					.addAbility("heavy_charged", 		ModStandAbilities.HEAVY_CHARGED, true)
					
					.addAbility("block", 				ModStandAbilities.BLOCK, true)
					.addAbility("quickstep", 			ModStandAbilities.QUICKSTEP, true)
					
//					.addAbility("grab_terrain", 		ModStandAbilities.GRAB_TERRAIN)
//					.addAbility("grab_release", 		ModStandAbilities.GRAB_RELEASE)
//					.addAbility("dodge", 				ModStandAbilities.DODGE)
//					.addAbility("manual_control",	 	ModStandAbilities.MANUAL_CONTROL)
//					.addAbility("swap_items", 			ModStandAbilities.SWAP_ITEMS)
//					.addAbility("item_lmb", 			ModStandAbilities.ITEM_LMB)
//					.addAbility("item_rmb", 			ModStandAbilities.ITEM_RMB)
//					.addAbility("leap", 				ModStandAbilities.STAND_LEAP)
//					.addAbility("star_finger", 			ModStandAbilities.SP_STAR_FINGER)
//					.addAbility("star_finger_swipe", 	ModStandAbilities.SP_STAR_FINGER_SWIPE)
//					.addAbility("inhale", 				ModStandAbilities.SP_INHALE)
//					.addAbility("time_stop", 			ModStandAbilities.TIME_STOP)

					, id));
}
