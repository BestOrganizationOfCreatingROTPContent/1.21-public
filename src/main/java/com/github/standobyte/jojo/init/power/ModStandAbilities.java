package com.github.standobyte.jojo.init.power;

import static com.github.standobyte.jojo.core.JojoRegistries.ABILITY_TYPES;

import com.github.standobyte.jojo.jojoimpl.stands._entitybase.StandBearingShotAbility;
import com.github.standobyte.jojo.jojoimpl.stands._entitybase.StandEntityBarrageAbility;
import com.github.standobyte.jojo.jojoimpl.stands._entitybase.StandEntityGrabAbility;
import com.github.standobyte.jojo.jojoimpl.stands._entitybase.StandEntityGrabReleaseAbility;
import com.github.standobyte.jojo.jojoimpl.stands._entitybase.StandEntityGrabThrowAbility;
import com.github.standobyte.jojo.jojoimpl.stands._entitybase.StandEntityHeavyPunchAbility;
import com.github.standobyte.jojo.jojoimpl.stands._entitybase.StandEntityHeavyPunchChargedAbility;
import com.github.standobyte.jojo.jojoimpl.stands._entitybase.StandEntityManualControlToggle;
import com.github.standobyte.jojo.jojoimpl.stands._entitybase.StandEntityPunchAbility;
import com.github.standobyte.jojo.jojoimpl.stands._entitybase.item.StandRightClickBlockAbility;
import com.github.standobyte.jojo.jojoimpl.stands._entitybase.item.StandUseItemAbility;
import com.github.standobyte.jojo.jojoimpl.stands._entitybase.item.SwapStandHandItemsAbility;
import com.github.standobyte.jojo.jojoimpl.stands._entitybase.item.SwapUserStandItemsAbility;
import com.github.standobyte.jojo.jojoimpl.stands._entitybase.item.TossStandItemAbility;
import com.github.standobyte.jojo.jojoimpl.stands.crazydiamond.CrazyDRepairItemAbility;
import com.github.standobyte.jojo.jojoimpl.stands.starplatinum.InhaleAbility;
import com.github.standobyte.jojo.jojoimpl.stands.starplatinum.StarFingerAbility;
import com.github.standobyte.jojo.jojoimpl.stands.starplatinum.StarFingerSwipeAbility;
import com.github.standobyte.jojo.jojoimpl.stands.theworld.TimeStopAbility;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;

import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModStandAbilities {
	public static final DeferredHolder<AbilityType<?>, AbilityType<StandEntityManualControlToggle>> MANUAL_CONTROL = ABILITY_TYPES.register(
			"stand_manual_control", key -> new AbilityType<>(key, StandEntityManualControlToggle::new));
	
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<SwapUserStandItemsAbility>> ITEMS_SWAP_W_USER = ABILITY_TYPES.register(
			"stand_items_swap_w_user", key -> new AbilityType<>(key, SwapUserStandItemsAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<SwapStandHandItemsAbility>> ITEMS_SWAP_HANDS = ABILITY_TYPES.register(
			"stand_items_swap_hands", key -> new AbilityType<>(key, SwapStandHandItemsAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<TossStandItemAbility>> ITEM_TOSS = ABILITY_TYPES.register(
			"stand_item_toss", key -> new AbilityType<>(key, TossStandItemAbility::new));
	
//	public static final DeferredHolder<AbilityType<?>, AbilityType<StandAttackWithItemAbility>> ITEM_ATTACK = ABILITY_TYPES.register(
//			"stand_item_attack", key -> new AbilityType<>(key, StandAttackWithItemAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<StandUseItemAbility>> ITEM_USE = ABILITY_TYPES.register(
			"stand_item_use", key -> new AbilityType<>(key, StandUseItemAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<StandRightClickBlockAbility>> BLOCK_USE = ABILITY_TYPES.register(
			"stand_block_use", key -> new AbilityType<>(key, StandRightClickBlockAbility::new));
	
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<StandEntityPunchAbility>> PUNCH = ABILITY_TYPES.register(
			"stand_punch", key -> new AbilityType<>(key, StandEntityPunchAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<StandEntityBarrageAbility>> BARRAGE = ABILITY_TYPES.register(
			"stand_barrage", key -> new AbilityType<>(key, StandEntityBarrageAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<StandEntityHeavyPunchAbility>> HEAVY_PUNCH = ABILITY_TYPES.register(
			"stand_heavy_punch", key -> new AbilityType<>(key, StandEntityHeavyPunchAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<StandEntityHeavyPunchChargedAbility>> HEAVY_CHARGED = ABILITY_TYPES.register(
			"stand_heavy_charged", key -> new AbilityType<>(key, StandEntityHeavyPunchChargedAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<StandEntityGrabAbility>> GRAB = ABILITY_TYPES.register(
			"stand_grab", key -> new AbilityType<>(key, StandEntityGrabAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<Ability>> GRAB_RELEASE = ABILITY_TYPES.register(
			"stand_grab_release", key -> new AbilityType<>(key, StandEntityGrabReleaseAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<StandEntityGrabThrowAbility>> GRAB_THROW = ABILITY_TYPES.register(
			"stand_grab_throw", key -> new AbilityType<>(key, StandEntityGrabThrowAbility::new));

	
	public static final DeferredHolder<AbilityType<?>, AbilityType<StarFingerAbility>> SP_STAR_FINGER = ABILITY_TYPES.register(
			"star_finger", key -> new AbilityType<>(key, StarFingerAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<StarFingerSwipeAbility>> SP_STAR_FINGER_SWIPE = ABILITY_TYPES.register(
			"star_finger_swipe", key -> new AbilityType<>(key, StarFingerSwipeAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<InhaleAbility>> SP_INHALE = ABILITY_TYPES.register(
			"inhale", key -> new AbilityType<>(key, InhaleAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<StandBearingShotAbility>> BEARING_SHOT = ABILITY_TYPES.register(
			"bearing_shot", key -> new AbilityType<>(key, StandBearingShotAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<TimeStopAbility>> TIME_STOP = ABILITY_TYPES.register(
			"time_stop", key -> new AbilityType<>(key, TimeStopAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<CrazyDRepairItemAbility>> CD_REPAIR_ITEM = ABILITY_TYPES.register(
			"repair_item", key -> new AbilityType<>(key, CrazyDRepairItemAbility::new));
	
	
	public static void load() {}
}
