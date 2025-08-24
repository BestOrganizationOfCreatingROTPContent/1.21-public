package com.github.standobyte.jojo.init.power;

import java.util.Collections;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.powersystem.MovesetBuilder;
import com.github.standobyte.jojo.powersystem.ability.controls.InputKey;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.standpower.StandStats;
import com.github.standobyte.jojo.powersystem.standpower.StandUnlockableSkill;
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
					
					// has a higher priority than regular item usage (added in addHumanoidStandStuff()) or charged heavy
					.addAbility("bearing_shot", ModStandAbilities.BEARING_SHOT)
					.withBind(InputKey.RMB, InputMethod.HOLD)
					
					.addHumanoidStandStuff()
					
					.addAbility("punch", ModStandAbilities.PUNCH, punch -> 
						Collections.addAll(punch.punchNames, "punch2", "punch3", "punch4")
					)
					.withBind(InputKey.LMB, InputMethod.CLICK)
					.addAbility("punch2", ModStandAbilities.PUNCH)
					.addAbility("punch3", ModStandAbilities.PUNCH)
					.addAbility("punch4", ModStandAbilities.PUNCH, 
							punch -> punch.setDefaultPhaseLength(ActionPhase.WINDUP, 5)
					)

					.addAbility("heavy_punch", ModStandAbilities.HEAVY_PUNCH)
					.withBind(InputKey.RMB, InputMethod.CLICK)
					.addAbility("heavy_punch2", ModStandAbilities.HEAVY_PUNCH)
					.addAbility("uppercut", ModStandAbilities.HEAVY_PUNCH)
					
					.addAbility("heavy_charged", ModStandAbilities.HEAVY_CHARGED)
					.withBind(InputKey.RMB, InputMethod.HOLD)
					
					.addAbility("barrage", ModStandAbilities.BARRAGE)
					.withBind(InputKey.LMB, InputMethod.HOLD)
					
					.addAbility("grab",ModStandAbilities.GRAB)
					.withBind(InputKey.RMB.withModifier(InputKey.Modifier.CONTROL), InputMethod.CLICK)
					
					.addAbility("grab_release", ModStandAbilities.GRAB_RELEASE)
					.withBind(InputKey.RMB.withModifier(InputKey.Modifier.CONTROL), InputMethod.CLICK)
					
					.addAbility("grabbed_throw", ModStandAbilities.GRAB_THROW)
					.withBind(InputKey.RMB, InputMethod.HOLD)
					
//					.addAbility("grab_punch", ModStandAbilities.PUNCH)
//					.addAbility("grab_punch2", ModStandAbilities.PUNCH)
//					.addAbility("grab_punch3", ModStandAbilities.PUNCH)
//					.addAbility("grab_punch4", ModStandAbilities.PUNCH)
//					.addAbility("grab_ground_slam", ModStandAbilities.HEAVY_PUNCH)
//					.addAbility("grab_barrage", ModStandAbilities.BARRAGE)
//					.addAbility("grab_terrain", ModStandAbilities.GRAB_TERRAIN)
//					.addAbility("terrain_throw", ModStandAbilities.GRAB_TERRAIN_THROW)
					
//					.addAbility("block", ModStandAbilities.BLOCK)
					
//					.addAbility("leap", ModStandAbilities.STAND_LEAP)
//					.addAbility("manual_control", ModStandAbilities.MANUAL_CONTROL)
//					.addAbility("swap_items", ModStandAbilities.SWAP_ITEMS)
//					.addAbility("item_lmb", ModStandAbilities.ITEM_LMB)
//					.addAbility("item_rmb", ModStandAbilities.ITEM_RMB)

//					.addAbility("ground_throw", ModStandAbilities.HEAVY_PUNCH)
//					.addAbility("retal_heavy", ModStandAbilities.HEAVY_PUNCH)
//					.addAbility("kick_barrage", ModStandAbilities.BARRAGE)
					
					
					.makeHotbar(0, InputKey.X, InputKey.C)
					
					.addAbility("placeholder1", ModStandAbilities._PLACEHOLDER)
					.inHotbar(0, InputMethod.CLICK)
					
					.addAbility("placeholder2", ModStandAbilities._PLACEHOLDER)
					.inHotbar(0, InputMethod.CLICK)
					
					.addAbility("placeholder3", ModStandAbilities._PLACEHOLDER)
					.inHotbar(0, InputMethod.CLICK)
					
					.addAbility("placeholder4", ModStandAbilities._PLACEHOLDER)
					.inHotbar(0, InputMethod.CLICK)
					
					.addAbility("placeholder5", ModStandAbilities._PLACEHOLDER)
					.inHotbar(0, InputMethod.CLICK)
					
					.addAbility("placeholder6", ModStandAbilities._PLACEHOLDER)
					.inHotbar(0, InputMethod.CLICK)
					
//					.addAbility("star_finger", ModStandAbilities.SP_STAR_FINGER)
//					.inHotbar(0, InputMethod.CLICK)
//					
//					.addAbility("star_finger_swipe", ModStandAbilities.SP_STAR_FINGER_SWIPE)
//					.inHotbarSlotVariation("star_finger", InputKey.Modifier.CONTROL, InputMethod.CLICK)
//					
//					.addAbility("inhale", ModStandAbilities.SP_INHALE)
//					.inHotbar(0, InputMethod.HOLD)
					
//					.addAbility("time_stop", ModStandAbilities.TIME_STOP)
					
					
					.addSkill(StandUnlockableSkill.startingAbility("punch"))
					.addSkill(StandUnlockableSkill.startingAbility("heavy_punch"))
					.addSkill(StandUnlockableSkill.unlockableAbility("uppercut", 1).prerequisiteSkill("heavy_punch"))
					.addSkill(StandUnlockableSkill.unlockableAbility("heavy_charged", 1).prerequisiteSkill("heavy_punch"))
					.addSkill(StandUnlockableSkill.startingAbility("barrage"))
					.addSkill(StandUnlockableSkill.unlockableAbility("grab", 1))
					.addSkill(StandUnlockableSkill.unlockableAbility("grabbed_throw", 1).prerequisiteSkill("grab"))
					.addSkill(StandUnlockableSkill.unlockableAbility("placeholder1", 1))
					.addSkill(StandUnlockableSkill.unlockableAbility("placeholder2", 1))
					.addSkill(StandUnlockableSkill.unlockableAbility("placeholder3", 1))
					.addSkill(StandUnlockableSkill.unlockableAbility("placeholder4", 1))
					.addSkill(StandUnlockableSkill.unlockableAbility("placeholder5", 1))
					.addSkill(StandUnlockableSkill.unlockableAbility("placeholder6", 1))

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
					
					.addHumanoidStandStuff()
					
					.addAbility("repair_item", ModStandAbilities.CD_REPAIR_ITEM)
					.withBind(InputKey.C, InputMethod.HOLD)

					, id));
}
