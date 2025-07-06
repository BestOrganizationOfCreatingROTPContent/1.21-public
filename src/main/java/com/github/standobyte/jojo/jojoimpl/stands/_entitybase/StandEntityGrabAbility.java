package com.github.standobyte.jojo.jojoimpl.stands._entitybase;

import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.mechanics.grab.LivingComponentGrab;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandOffsetFromUser;
import com.github.standobyte.jojo.util.StandUtil;
import com.github.standobyte.jojo.util.target.ActionTarget;
import com.github.standobyte.jojo.util.target.ActionTarget.TargetType;
import com.github.standobyte.jojo.util.target.AimingEntity;
import com.github.standobyte.jojo.util.target.HitResultUtil;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class StandEntityGrabAbility extends StandEntityAbility {

	public StandEntityGrabAbility(AbilityId abilityId) {
		super(abilityId);
		setDefaultPhaseLength(ActionPhase.WINDUP, 10);
		setDefaultPhaseLength(ActionPhase.PERFORM, 6);
		setDefaultPhaseLength(ActionPhase.RECOVERY, 16);
	}
	
	@Override
	public boolean isAbilityAvailable(Power<?> context) {
		return super.isAbilityAvailable(context) && StandUtil.getStandGrabTarget(context) == null;
	}
	

	@Override
	public EntityActionInstance createActionObj() {
		return new StandEntityGrab(this);
	}

	public static class StandEntityGrab extends EntityActionInstance {

		public StandEntityGrab(EntityActionType ability) {
			super(ability);
		}
		
		@Override
		public void onActionSet(EntityActionInstance prevAction) {
			setStandOffset(0, 2, StandOffsetFromUser.OffsetMode.HEAD_XY, false);
			keepStandAimedAtTarget();
			aimAs = AimingEntity.STAND;
		}

		@Override
		public void actionPerformStart() {
			Level level = level();
			if (performer instanceof StandEntity standEntity && standEntity.isHandFree(InteractionHand.OFF_HAND)) {
				ActionTarget target = HitResultUtil.clipEntityLook(standEntity, 
						entity -> !entity.is(standEntity.getUser()) && StandEntityPunchAbility.canStandHit(standEntity, entity) && !(
								entity instanceof LivingEntity living && (
										LivingComponentGrab.getGrabbedEntity(living) != null
										|| LivingComponentGrab.getGrabbingEntity(living) != null)), 
						0);
				if (!level.isClientSide()) {
					if (target.getType() == TargetType.ENTITY && target.getEntity() instanceof LivingEntity targetLiving) {
						LivingComponentGrab standGrab = performer.getData(ModDataAttachmentTypes.LIVING_GRAB.get());
						standGrab.setGrabbedEntity(targetLiving);
					}
				}
				
				if (target.getType() == TargetType.ENTITY) {
					standRotationTarget = target;
				}
				else {
					aimAs = AimingEntity.PLAYER;
				}
			}
		}

	}

}
