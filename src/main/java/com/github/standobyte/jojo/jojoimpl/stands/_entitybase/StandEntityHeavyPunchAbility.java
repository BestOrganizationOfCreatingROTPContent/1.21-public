package com.github.standobyte.jojo.jojoimpl.stands._entitybase;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.sound.ClientsideSoundsHelper;
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

public class StandEntityHeavyPunchAbility extends StandEntityAbility {

	public StandEntityHeavyPunchAbility(AbilityId abilityId) {
		super(abilityId);
		setDefaultPhaseLength(ActionPhase.WINDUP, 10);
		setDefaultPhaseLength(ActionPhase.PERFORM, 6);
		setDefaultPhaseLength(ActionPhase.RECOVERY, 16);
	}
	
	
	@Override
	public EntityActionInstance createActionObj() {
		return new StandEntityHeavyPunch(this);
	}
	
	public static class StandEntityHeavyPunch extends EntityActionInstance {
		protected boolean playedSwingSound;
		protected boolean playedStandCrySound;

		public StandEntityHeavyPunch(EntityActionType ability) {
			super(ability);
		}
		
		@Override
		public void onActionSet(EntityActionInstance prevAction) {
			setStandOffset(0, 2, StandOffsetFromUser.OffsetMode.HEAD_XY, false);
			keepStandAimedAtTarget();
			aimAs = AimingEntity.STAND;
			tossStandHeldItems(EquipmentSlot.OFFHAND, EquipmentSlot.MAINHAND);
		}
		
		@Override
		public void actionTick() {
			Level level = performer.level();
			if (level.isClientSide() && ClientGlobals.canHearStands && !(playedSwingSound && playedStandCrySound) && performer instanceof StandEntity stand) {
				if (!playedSwingSound) {
					// how many ticks are left before the end of the 'perform' phase (when actionPerformEnd() is called)
					int ticksDiff = (int) (calcFullTicks(ActionPhase.PERFORM, phasesLength.get(ActionPhase.PERFORM)) - getFullTicksPassed());
					if (ticksDiff <= 5) {
						level.playLocalSound(stand.getX(), stand.getEyeY(), stand.getZ(), ClientsideSoundsHelper.withStandSkin(
								ModSoundEvents.STAND_PUNCH_HEAVY_SWING.get(), stand.getStandId(), stand.getStandSkin()), 
								stand.getSoundSource(), 1, 1, false);
						playedSwingSound = true;
					}
				}
				
				if (!playedStandCrySound) {
					ClientsideSoundsHelper.playEntityLingeringSound(stand, ClientsideSoundsHelper.withStandSkin(
							ModSoundEvents.STAND_PUNCH_HEAVY_CRY.get(), stand.getStandId(), stand.getStandSkin()), 
							stand.getSoundSource(), 1, 1, level);
					playedStandCrySound = true;
				}
			}
		}

		@Override
		public void actionPerformStart() {
			Level level = level();
			if (performer instanceof StandEntity stand) {
				ActionTarget target = HitResultUtil.clipEntityLook(stand, entity -> StandEntityPunchAbility.canStandHit(stand, entity), 0);
				if (!level.isClientSide()) {
					if (target.getType() == TargetType.ENTITY && target.getEntity() instanceof LivingEntity targetLiving) {
						var damageType = DamageUtil.type(level, ModDamageTypes.STAND_ATTACK);
						DamageSource dmgSource = new DamageSource(damageType, performer);
						((RipplesModifiedDamageSource) dmgSource).jojo_ripples$modifyKnockback(1f, 1);
						float dmgAmount = 13.875f;
						standEntityAttack(stand, targetLiving, dmgSource, dmgAmount);
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
