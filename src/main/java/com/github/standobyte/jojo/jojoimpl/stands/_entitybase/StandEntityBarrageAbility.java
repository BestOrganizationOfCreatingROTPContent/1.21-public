package com.github.standobyte.jojo.jojoimpl.stands._entitybase;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.sound.ClientsideSoundsHelper;
import com.github.standobyte.jojo.client.sound.sounds.EntityStoppableSoundInstance;
import com.github.standobyte.jojo.init.ModDamageTypes;
import com.github.standobyte.jojo.init.ModSoundEvents;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandOffsetFromUser;
import com.github.standobyte.jojo.util.damage.DamageUtil;
import com.github.standobyte.jojo.util.damage.RipplesModifiedDamageSource;
import com.github.standobyte.jojo.util.target.ActionTarget;
import com.github.standobyte.jojo.util.target.ActionTarget.TargetType;
import com.github.standobyte.jojo.util.target.AimingEntity;
import com.github.standobyte.jojo.util.target.HitResultUtil;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class StandEntityBarrageAbility extends StandEntityAbility {

	public StandEntityBarrageAbility(AbilityId abilityId) {
		super(abilityId);
		setDefaultPhaseLength(ActionPhase.PERFORM, 100);
		setDefaultPhaseLength(ActionPhase.RECOVERY, 10);
		noFinisherBarDecay = true;
	}
	
	
	@Override
	public EntityActionInstance createActionObj() {
		return new StandEntityBarrage(this);
	}
	
	public static class StandEntityBarrage extends EntityActionInstance {

		public StandEntityBarrage(EntityActionType ability) {
			super(ability);
		}
		
		@Override
		public void onActionSet(EntityActionInstance prevAction) {
			setStandOffset(0, 1.5, StandOffsetFromUser.Rotations.HEAD_XY, true);
			aimAs = AimingEntity.STAND;
			Level level = performer.level();
			if (performer instanceof StandEntity stand) {
				if (level.isClientSide()) {
					EntityStoppableSoundInstance sound = new EntityStoppableSoundInstance(ClientsideSoundsHelper.withStandSkin(
							ModSoundEvents.STAND_BARRAGE_CRY.get(), stand.getStandId(), stand.getStandSkin()), 
							stand.getSoundSource(), 1, 1, stand, level.random.nextLong(), () -> this.phase != ActionPhase.PERFORM);
					ClientsideSoundsHelper.playNonVanillaClassSound(sound);
				}
				tossStandHeldItems(EquipmentSlot.OFFHAND, EquipmentSlot.MAINHAND);
			}
		}
		
		@Override
		public void onSetPhase(ActionPhase newPhase) {
			userWalkSpeed = newPhase == ActionPhase.PERFORM ? 0.6f : 1;
		}

		@Override
		public void actionTick() {
			if (getPhase() == ActionPhase.PERFORM && performer instanceof StandEntity stand) {
				Level level = performer.level();
				if (level.isClientSide()) {
					if (ClientGlobals.canHearStands) {
						level.playLocalSound(stand.getX(), stand.getEyeY(), stand.getZ(), ClientsideSoundsHelper.withStandSkin(
								ModSoundEvents.STAND_PUNCH_BARRAGE_SWING.get(), stand.getStandId(), stand.getStandSkin()), 
								stand.getSoundSource(), 1, 1, false);
					}
				}
				else {
					ActionTarget target = HitResultUtil.clipEntityLook(stand, entity -> StandEntityPunchAbility.canStandHit(stand, entity), 0);
					if (target.getType() == TargetType.ENTITY && target.getEntity() instanceof LivingEntity targetLiving) {
						var damageType = DamageUtil.type(level, ModDamageTypes.STAND_ATTACK);
						DamageSource dmgSource = new DamageSource(damageType, performer);
						((RipplesModifiedDamageSource) dmgSource).jojo_ripples$modifyKnockback(0, 0.1f);
						float dmgAmount = 1;
						standEntityAttack(stand, targetLiving, dmgSource, dmgAmount);
					}
				}
			}
		}
		
		@Override
		public void onButtonStopHold() {
			if (getPhase() != ActionPhase.RECOVERY) {
				startPhase(ActionPhase.RECOVERY);
				syncPhaseChanges();
			}
		}
		
		@Override
		public boolean canBeCancelledInto(EntityActionType cancellingAbility) {
			return cancellingAbility != this.ability;
		}
		
	}

}
