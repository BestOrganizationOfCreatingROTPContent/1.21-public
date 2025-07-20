package com.github.standobyte.jojo.jojoimpl.stands._entitybase.item;

import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.util.StandUtil;
import com.github.standobyte.jojo.util.target.ActionTarget.TargetType;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class StandRightClickBlockAbility extends Ability {

	public StandRightClickBlockAbility(AbilityId abilityId) {
		super(abilityId);
	}
	
	@Override
	public boolean isAbilityAvailable(Power<?> context) {
		if (super.isAbilityAvailable(context)) {
			StandEntity standEntity = StandUtil.getSummonedStand(context);
			return standEntity != null && standEntity.isManuallyControlled() && 
					LivingComponentAction.getAim(standEntity).getTarget().getType() == TargetType.BLOCK;
		}
		return false;
	}
	
	@Override
	public void onClick(Level level, LivingEntity user, 
			FriendlyByteBuf extraClientInput, float clickHoldResolveTime) {
		if (!level.isClientSide()) {
			StandEntity standEntity = StandUtil.getSummonedStand(user);
			if (standEntity != null) {
//				ItemStack lUserItem = user.getOffhandItem();
//				ItemStack rUserItem = user.getMainHandItem();
//				ItemStack lStandItem = standEntity.getOffhandItem();
//				ItemStack rStandItem = standEntity.getMainHandItem();
//				
//				if (!lUserItem.isEmpty()) {
//					LivingComponentGrab standGrab = standEntity.getData(ModDataAttachmentTypes.LIVING_GRAB.get());
//					if (standGrab != null) {
//						standGrab.setGrabbedEntity(null);
//					}
//				}
//				
//				standEntity.setItemInHand(InteractionHand.OFF_HAND, lUserItem);
//				standEntity.setItemInHand(InteractionHand.MAIN_HAND, rUserItem);
//				user.setItemInHand(InteractionHand.OFF_HAND, lStandItem);
//				user.setItemInHand(InteractionHand.MAIN_HAND, rStandItem);
			}
		}
	}

}
