package com.github.standobyte.jojo.init.power;

import java.util.Collections;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.powersystem.MovesetBuilder;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.standpower.StandStats;
import com.github.standobyte.jojo.powersystem.standpower.entity.EntityStandType;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// XXX test the stand datapack configs
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

					new MovesetBuilder()
					.addAbility("punch", 				ModStandAbilities.PUNCH, true, 
							punch -> Collections.addAll(punch.punchNames, "punch2", "punch3", "punch4")
					)
					.addAbility("punch2", 				ModStandAbilities.PUNCH, false)
					.addAbility("punch3", 				ModStandAbilities.PUNCH, false)
					.addAbility("punch4", 				ModStandAbilities.PUNCH, false, 
							punch -> punch.setDefaultPhaseLength(ActionPhase.WINDUP, 5)
					)

					.addAbility("heavy_punch", 			ModStandAbilities.HEAVY_PUNCH, true)
					.addAbility("heavy_punch2", 		ModStandAbilities.HEAVY_PUNCH, false)
					.addAbility("uppercut", 			ModStandAbilities.HEAVY_PUNCH, false)
					
					.addAbility("heavy_charged", 		ModStandAbilities.HEAVY_CHARGED, true)
					
					.addAbility("barrage", 				ModStandAbilities.BARRAGE, true)
					
					.addAbility("grab", 				ModStandAbilities.GRAB, true)
					.addAbility("grab_release", 		ModStandAbilities.GRAB_RELEASE)
					.addAbility("grabbed_throw", 		ModStandAbilities.GRAB_THROW, false)
//					.addAbility("grab_punch", 			ModStandAbilities.PUNCH, false)
//					.addAbility("grab_punch2", 			ModStandAbilities.PUNCH, false)
//					.addAbility("grab_punch3", 			ModStandAbilities.PUNCH, false)
//					.addAbility("grab_punch4", 			ModStandAbilities.PUNCH, false)
//					.addAbility("grab_ground_slam", 	ModStandAbilities.HEAVY_PUNCH, false)
//					.addAbility("grab_barrage", 		ModStandAbilities.BARRAGE, false)
//					.addAbility("grab_terrain", 		ModStandAbilities.GRAB_TERRAIN)
//					.addAbility("terrain_throw", 		ModStandAbilities.GRAB_TERRAIN_THROW)
					
//					.addAbility("block", 				ModStandAbilities.BLOCK, true)
					
//					.addAbility("leap", 				ModStandAbilities.STAND_LEAP)
//					.addAbility("manual_control",	 	ModStandAbilities.MANUAL_CONTROL)
//					.addAbility("swap_items", 			ModStandAbilities.SWAP_ITEMS)
//					.addAbility("item_lmb", 			ModStandAbilities.ITEM_LMB)
//					.addAbility("item_rmb", 			ModStandAbilities.ITEM_RMB)

//					.addAbility("ground_throw", 		ModStandAbilities.HEAVY_PUNCH, false)
//					.addAbility("retal_heavy", 			ModStandAbilities.HEAVY_PUNCH, false)
//					.addAbility("kick_barrage", 		ModStandAbilities.BARRAGE, false)
					
//					.addAbility("star_finger", 			ModStandAbilities.SP_STAR_FINGER)
//					.addAbility("star_finger_swipe", 	ModStandAbilities.SP_STAR_FINGER_SWIPE)
//					.addAbility("inhale", 				ModStandAbilities.SP_INHALE)
//					.addAbility("time_stop", 			ModStandAbilities.TIME_STOP)

					, id));
	
	public static final DeferredHolder<StandType, EntityStandType> CRAZY_DIAMOND = DEFAULT_STANDS.register(
			"crazy_diamond", id -> 
			new EntityStandType(
					new StandStats.Builder()
					.power(17.0)
					.speed(16.5)
					.range(2, 4)
					.durability(13.0)
					.precision(12.0)
					.build(),

					new MovesetBuilder()
					.addAbility("barrage", 				ModStandAbilities.BARRAGE, true)
					.addAbility("repair_item", 			ModStandAbilities.CD_REPAIR_ITEM, false)

					, id));
}
