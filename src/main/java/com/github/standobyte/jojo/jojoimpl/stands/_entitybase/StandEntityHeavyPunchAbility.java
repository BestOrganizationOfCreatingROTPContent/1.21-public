package com.github.standobyte.jojo.jojoimpl.stands._entitybase;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.sound.ClientsideSoundsHelper;
import com.github.standobyte.jojo.init.ModDamageTypes;
import com.github.standobyte.jojo.init.ModSoundEvents;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandOffsetFromUser;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandStatFormulas;
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

	public StandEntityHeavyPunchAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId);
		setDefaultPhaseLength(ActionPhase.WINDUP, StandStatFormulas.getHeavyAttackWindup(8, 0));
		setDefaultPhaseLength(ActionPhase.PERFORM, 5);
		setDefaultPhaseLength(ActionPhase.RECOVERY, 16);
		noFinisherBarDecay = true;
	}
	
	
	@Override
	public EntityActionInstance createActionObj() {
		return new StandEntityHeavyPunch(this);
	}
	
	@Override
	public void initActionFromConfig(EntityActionInstance action, Level level, 
			LivingEntity powerUser, LivingEntity performer) {
		super.initActionFromConfig(action, level, powerUser, performer);
		if (!level.isClientSide() && performer instanceof StandEntity stand) {
			action.phasesLength.put(ActionPhase.WINDUP, StandStatFormulas.getHeavyAttackWindup(stand.getAttackSpeed(), stand.getFinisherMeter()));
		}
	}
	
	public static class StandEntityHeavyPunch extends EntityActionInstance {
		protected float finisherValue;
		protected boolean playedSwingSound;
		protected boolean playedStandCrySound;

		public StandEntityHeavyPunch(EntityActionType ability) {
			super(ability);
		}
		
		@Override
		public void onActionSet(EntityActionInstance prevAction) {
			setStandOffset(0, 2, StandOffsetFromUser.Rotations.HEAD_XY, false);
			keepStandAimedAtTarget();
			aimAs = AimingEntity.STAND;
			if (performer instanceof StandEntity stand) {
				finisherValue = stand.getFinisherMeter();
			}
			tossStandHeldItems(EquipmentSlot.OFFHAND, EquipmentSlot.MAINHAND);
		}
		
		@Override
		public void actionTick() {
			Level level = performer.level();
			if (level.isClientSide() && ClientGlobals.canHearStands && !(playedSwingSound && playedStandCrySound) && performer instanceof StandEntity stand) {
				if (!playedSwingSound) {
					// how many ticks are left before the start of the 'perform' phase (when actionPerformStart() is called)
					int ticksDiff = (int) (calcFullTicks(ActionPhase.PERFORM, 0) - getFullTicksPassed());
					if (ticksDiff <= 4) {
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
						float dmgAmount = StandStatFormulas.getHeavyAttackDamage(stand.getAttackDamage());
						standEntityAttack(stand, targetLiving, dmgSource, dmgAmount);
					}
					StandPower standPower = StandPower.get(getPowerUser());
					standPower.consumeStamina(10);
					stand.consumeFinisherMeter(1.0001f);
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
