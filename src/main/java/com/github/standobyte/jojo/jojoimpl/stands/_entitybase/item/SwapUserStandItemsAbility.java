package com.github.standobyte.jojo.jojoimpl.stands._entitybase.item;

import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.mechanics.grab.LivingComponentGrab;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.condition.ConditionCheck;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.util.MathUtil;
import com.github.standobyte.jojo.util.StandUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SwapUserStandItemsAbility extends Ability {

	public SwapUserStandItemsAbility(AbilityId abilityId) {
		super(abilityId);
	}
	
	@Override
	public ConditionCheck checkSpecificConditions(Power<?> context) {
		StandEntity stand = StandUtil.getSummonedStand(context);
		LivingEntity user = context.getUser();
		if (stand == null || user == null) return ConditionCheck.NEGATIVE;
		
		if (!stand.isFollowingUser() && MathUtil.getAABBDistance(stand.getBoundingBox(), user.getBoundingBox()) > 4.5) {
			return ConditionCheck.createNegative("stand_user_too_far");
		}
		
		return super.checkSpecificConditions(context);
	}
	
	@Override
	public void onClick(Level level, LivingEntity user, FriendlyByteBuf extraClientInput) {
		if (!level.isClientSide()) {
			StandEntity stand = StandUtil.getSummonedStand(user);
			if (stand != null) {
				ItemStack lUserItem = user.getOffhandItem();
				ItemStack rUserItem = user.getMainHandItem();
				int lUserItemCount = lUserItem.getCount();
				int rUserItemCount = rUserItem.getCount();
				
				 // if the player is holding any items, give them to the stand
				if (!lUserItem.isEmpty() || !rUserItem.isEmpty()) {
					stand.addItem(rUserItem);
					stand.addItem(lUserItem);
					boolean gaveSomethingToStand = lUserItem.getCount() < lUserItemCount || rUserItem.getCount() < rUserItemCount;
					if (!gaveSomethingToStand) {
						// swap the player's non-empty items with stand's
						if (!lUserItem.isEmpty()) swapItemsInHand(stand, user, InteractionHand.OFF_HAND);
						if (!rUserItem.isEmpty()) swapItemsInHand(stand, user, InteractionHand.MAIN_HAND);
					}
				}
				// or, if the player's hands are empty, *take* both items from the stand
				else {
					swapItemsInHand(stand, user, InteractionHand.OFF_HAND);
					swapItemsInHand(stand, user, InteractionHand.MAIN_HAND);
				}

				if (!stand.getOffhandItem().isEmpty()) {
					LivingComponentGrab standGrab = stand.getData(ModDataAttachmentTypes.LIVING_GRAB.get());
					if (standGrab != null) {
						standGrab.setGrabbedEntity(null);
					}
				}
			}
		}
	}
	
	public static void swapItemsInHand(LivingEntity stand, LivingEntity user, InteractionHand hand) {
		ItemStack userItem = user.getItemInHand(hand);
		ItemStack standItem = stand.getItemInHand(hand);
		stand.setItemInHand(hand, userItem);
		user.setItemInHand(hand, standItem);
	}
}
