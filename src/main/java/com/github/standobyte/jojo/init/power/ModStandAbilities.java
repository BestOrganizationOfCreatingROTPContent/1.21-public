package com.github.standobyte.jojo.init.power;

import static com.github.standobyte.jojo.core.JojoRegistries.ABILITY_TYPES;

import com.github.standobyte.jojo.jojoimpl.stands.starplatinum.InhaleAbility;
import com.github.standobyte.jojo.jojoimpl.stands.starplatinum.StarFingerAbility;
import com.github.standobyte.jojo.jojoimpl.stands.starplatinum.StarFingerSwipeAbility;
import com.github.standobyte.jojo.jojoimpl.stands.theworld.TimeStopAbility;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.standpower.StandAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.abilities.StandEntityBarrageAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.abilities.StandEntityBlockAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.abilities.StandEntityDodgeAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.abilities.StandEntityGrabAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.abilities.StandEntityGrabReleaseAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.abilities.StandEntityGrabTerrainAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.abilities.StandEntityGrabThrowAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.abilities.StandEntityHeavyPunchAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.abilities.StandEntityHeavyPunchChargedAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.abilities.StandEntityItemLmbAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.abilities.StandEntityItemRmbAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.abilities.StandEntityLeapAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.abilities.StandEntityManualControlAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.abilities.StandEntityPunchAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.abilities.StandEntityQuickstepAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.abilities.StandEntitySwapItemsAbility;

import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModStandAbilities {
	public static final DeferredHolder<AbilityType<?>, AbilityType<StandAbility>> PUNCH = ABILITY_TYPES.register(
			"stand_punch", key -> new AbilityType<>(key, StandEntityPunchAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<StandAbility>> BARRAGE = ABILITY_TYPES.register(
			"stand_barrage", key -> new AbilityType<>(key, StandEntityBarrageAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<StandAbility>> HEAVY_PUNCH = ABILITY_TYPES.register(
			"stand_heavy_punch", key -> new AbilityType<>(key, StandEntityHeavyPunchAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<StandAbility>> HEAVY_CHARGED = ABILITY_TYPES.register(
			"stand_heavy_charged", key -> new AbilityType<>(key, StandEntityHeavyPunchChargedAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<StandAbility>> GRAB = ABILITY_TYPES.register(
			"stand_grab", key -> new AbilityType<>(key, StandEntityGrabAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<StandAbility>> GRABBED_RELEASE = ABILITY_TYPES.register(
			"stand_grabbed_release", key -> new AbilityType<>(key, StandEntityGrabReleaseAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<StandAbility>> GRABBED_THROW = ABILITY_TYPES.register(
			"stand_grabbed_throw", key -> new AbilityType<>(key, StandEntityGrabThrowAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<StandAbility>> BLOCK = ABILITY_TYPES.register(
			"stand_block", key -> new AbilityType<>(key, StandEntityBlockAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<StandAbility>> QUICKSTEP = ABILITY_TYPES.register(
			"stand_quickstep", key -> new AbilityType<>(key, StandEntityQuickstepAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<StandAbility>> DODGE = ABILITY_TYPES.register(
			"stand_dodge", key -> new AbilityType<>(key, StandEntityDodgeAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<StandAbility>> MANUAL_CONTROL = ABILITY_TYPES.register(
			"stand_manual_control", key -> new AbilityType<>(key, StandEntityManualControlAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<StandAbility>> SWAP_ITEMS = ABILITY_TYPES.register(
			"stand_swap_items", key -> new AbilityType<>(key, StandEntitySwapItemsAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<StandAbility>> ITEM_LMB = ABILITY_TYPES.register(
			"stand_item_lmb", key -> new AbilityType<>(key, StandEntityItemLmbAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<StandAbility>> ITEM_RMB = ABILITY_TYPES.register(
			"stand_item_rmb", key -> new AbilityType<>(key, StandEntityItemRmbAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<StandAbility>> STAND_LEAP = ABILITY_TYPES.register(
			"stand_leap", key -> new AbilityType<>(key, StandEntityLeapAbility::new));

	
	public static final DeferredHolder<AbilityType<?>, AbilityType<StandAbility>> GRAB_TERRAIN = ABILITY_TYPES.register(
			"stand_grab_terrain", key -> new AbilityType<>(key, StandEntityGrabTerrainAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<StandAbility>> SP_STAR_FINGER = ABILITY_TYPES.register(
			"star_finger", key -> new AbilityType<>(key, StarFingerAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<StandAbility>> SP_STAR_FINGER_SWIPE = ABILITY_TYPES.register(
			"star_finger_swipe", key -> new AbilityType<>(key, StarFingerSwipeAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<StandAbility>> SP_INHALE = ABILITY_TYPES.register(
			"inhale", key -> new AbilityType<>(key, InhaleAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<StandAbility>> TIME_STOP = ABILITY_TYPES.register(
			"time_stop", key -> new AbilityType<>(key, TimeStopAbility::new));
	
	
	public static void load() {}
}
