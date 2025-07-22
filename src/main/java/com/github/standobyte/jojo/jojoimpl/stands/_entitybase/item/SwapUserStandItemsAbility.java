package com.github.standobyte.jojo.jojoimpl.stands._entitybase.item;

import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.mechanics.grab.LivingComponentGrab;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
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
	public void onClick(Level level, LivingEntity user, FriendlyByteBuf extraClientInput) {
		if (!level.isClientSide()) {
			StandEntity standEntity = StandUtil.getSummonedStand(user);
			if (standEntity != null) {
				ItemStack lUserItem = user.getOffhandItem();
				ItemStack rUserItem = user.getMainHandItem();
				ItemStack lStandItem = standEntity.getOffhandItem();
				ItemStack rStandItem = standEntity.getMainHandItem();
				
				if (!lUserItem.isEmpty()) {
					LivingComponentGrab standGrab = standEntity.getData(ModDataAttachmentTypes.LIVING_GRAB.get());
					if (standGrab != null) {
						standGrab.setGrabbedEntity(null);
					}
				}
				
				standEntity.setItemInHand(InteractionHand.OFF_HAND, lUserItem);
				standEntity.setItemInHand(InteractionHand.MAIN_HAND, rUserItem);
				user.setItemInHand(InteractionHand.OFF_HAND, lStandItem);
				user.setItemInHand(InteractionHand.MAIN_HAND, rStandItem);
			}
		}
	}
}
