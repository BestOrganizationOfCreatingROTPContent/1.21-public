package com.github.standobyte.jojo.jojoimpl.stands._entitybase.item;

import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;
import com.github.standobyte.jojo.util.StandUtil;

import net.minecraft.world.item.ItemStack;

public class StandAttackWithItemAbility extends StandEntityAbility {

	public StandAttackWithItemAbility(AbilityId abilityId) {
		super(abilityId);
	}
	
	@Override
	public boolean isAbilityAvailable(Power<?> context) {
		if (super.isAbilityAvailable(context)) {
			StandEntity standEntity = StandUtil.getSummonedStand(context);
			if (standEntity != null) {
				ItemStack rItem = standEntity.getMainHandItem();
				return !rItem.isEmpty();
			}
		}
		return false;
	}
	
	
	@Override
	public EntityActionInstance createActionObj() {
		return new StandItemAttack(this);
	}
	
	public static class StandItemAttack extends EntityActionInstance {

		public StandItemAttack(EntityActionType ability) {
			super(ability);
		}
		
		@Override
		public void onActionSet(EntityActionInstance prevAction) {
//			setStandOffset(0, 1.5, StandOffsetFromUser.OffsetMode.HEAD_XY, true);
//			aimAs = AimingEntity.STAND;
//			Level level = performer.level();
//			if (performer instanceof StandEntity stand) {
//				if (level.isClientSide()) {
//					EntityStoppableSoundInstance sound = new EntityStoppableSoundInstance(ClientsideSoundsHelper.withStandSkin(
//							ModSoundEvents.STAND_BARRAGE_CRY.get(), stand.getStandId(), stand.getStandSkin()), 
//							stand.getSoundSource(), 1, 1, stand, level.random.nextLong(), () -> this.phase != ActionPhase.PERFORM);
//					ClientsideSoundsHelper.playNonVanillaClassSound(sound);
//				}
//				tossStandHeldItems(EquipmentSlot.OFFHAND, EquipmentSlot.MAINHAND);
//			}
		}
		
		@Override
		public void onSetPhase(ActionPhase newPhase) {
//			userWalkSpeed = newPhase == ActionPhase.PERFORM ? 0.6f : 1;
		}

		@Override
		public void actionTick() {
//			if (getPhase() == ActionPhase.PERFORM && performer instanceof StandEntity stand) {
//				Level level = performer.level();
//				if (level.isClientSide()) {
//					if (ClientGlobals.canHearStands) {
//						level.playLocalSound(stand.getX(), stand.getEyeY(), stand.getZ(), ClientsideSoundsHelper.withStandSkin(
//								ModSoundEvents.STAND_PUNCH_BARRAGE_SWING.get(), stand.getStandId(), stand.getStandSkin()), 
//								stand.getSoundSource(), 1, 1, false);
//					}
//				}
//				else {
//					ActionTarget target = HitResultUtil.clipEntityLook(stand, entity -> StandEntityPunchAbility.canStandHit(stand, entity), 0);
//					if (target.getType() == TargetType.ENTITY && target.getEntity() instanceof LivingEntity targetLiving) {
//						var damageType = DamageUtil.type(level, ModDamageTypes.STAND_ATTACK);
//						DamageSource dmgSource = new DamageSource(damageType, performer);
//						((RipplesModifiedDamageSource) dmgSource).jojo_ripples$modifyKnockback(0, 0.1f);
//						float dmgAmount = 1;
//						standEntityAttack(stand, targetLiving, dmgSource, dmgAmount);
//					}
//				}
//			}
		}
		
		@Override
		public void onButtonStopHold() {
//			if (getPhase() != ActionPhase.RECOVERY) {
//				startPhase(ActionPhase.RECOVERY);
//				syncPhaseChanges();
//			}
		}
		
		@Override
		public boolean canBeCancelledInto(EntityActionType cancellingAbility) {
			return true;
//			return cancellingAbility != this.ability;
		}
		
	}
	
}
