package com.github.standobyte.jojo.jojoimpl.stands._entitybase;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.sound.ClientsideSoundsHelper;
import com.github.standobyte.jojo.client.sound.sounds.EntityStoppableSoundInstance;
import com.github.standobyte.jojo.init.ModDamageTypes;
import com.github.standobyte.jojo.init.ModSoundEvents;
import com.github.standobyte.jojo.mechanics.ServerBlockDestroyTracker;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.condition.ConditionCheck;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandOffsetFromUser;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandStatFormulas;
import com.github.standobyte.jojo.util.StandUtil;
import com.github.standobyte.jojo.util.StandUtil.StandStat;
import com.github.standobyte.jojo.util.damage.DamageUtil;
import com.github.standobyte.jojo.util.damage.RipplesModifiedDamageSource;
import com.github.standobyte.jojo.util.target.ActionTarget;
import com.github.standobyte.jojo.util.target.AimingEntity;
import com.github.standobyte.jojo.util.target.HitResultUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public class StandEntityBarrageAbility extends StandEntityAbility {

	public StandEntityBarrageAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId);
		setDefaultPhaseLength(ActionPhase.PERFORM, StandStatFormulas.getBarrageMaxDuration(8));
		setDefaultPhaseLength(ActionPhase.RECOVERY, 10);
		noFinisherBarDecay = true;
	}
	
	@Override
	public ConditionCheck checkSpecificConditions(Power<?> context) {
		double standAttackSpeed = StandUtil.getPhysicalStatValue((StandPower) context, StandStat.ATTACK_SPEED);
		float hits = StandStatFormulas.getBarrageHitsPerSecond(standAttackSpeed);
		if (hits <= 0) {
			return ConditionCheck.createNegative("stand_too_slow");
		}
		return super.checkSpecificConditions(context);
	}
	
	
	@Override
	public EntityActionInstance createActionObj() {
		return new StandEntityBarrage(this);
	}
	
	@Override
	public void initActionFromConfig(EntityActionInstance action, Level level, 
			LivingEntity powerUser, LivingEntity performer) {
		super.initActionFromConfig(action, level, powerUser, performer);
		if (!level.isClientSide() && performer instanceof StandEntity stand) {
//			if (powerUser instanceof Player player && player.getAbilities().instabuild) {
//				action.phasesLength.put(ActionPhase.PERFORM, 999999);
//				action.phasesLength.put(ActionPhase.RECOVERY, 0);
//			}
//			else {
				action.phasesLength.put(ActionPhase.PERFORM, StandStatFormulas.getBarrageMaxDuration(stand.getDurability()));
//			}
		}
	}
	
	public static class StandEntityBarrage extends EntityActionInstance {
		public int hitsThisTick;

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
				float hitsPerSec = StandStatFormulas.getBarrageHitsPerSecond(stand.getAttackSpeed());
				float hitsPerTick = hitsPerSec / 20;
				int curTick = (curPhaseTick - 1) % 20 + 1; // 1~20
				hitsThisTick = (int) (hitsPerTick * curTick) - (int) (hitsPerTick * (curTick - 1));
				
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
					switch (target.getType()) {
						case ENTITY -> dealDamage(target, level, stand);
						case BLOCK -> mineBlock(target, level, stand);
						default -> {}
					}
					StandPower standPower = StandPower.get(getPowerUser());
					standPower.consumeStamina(4);
				}
			}
		}
		
		@Override
		public void onButtonStopHold() {
			if (getPhase() != ActionPhase.RECOVERY) {
				setPhaseStart(ActionPhase.RECOVERY);
				syncPhaseChanges();
			}
		}
		
		@Override
		public boolean canBeCancelledInto(EntityActionType cancellingAbility) {
			return cancellingAbility != this.ability;
		}
		
		protected void dealDamage(ActionTarget entityTarget, Level level, StandEntity stand) {
			if (entityTarget.getEntity() instanceof LivingEntity targetLiving) {
				var damageType = DamageUtil.type(level, ModDamageTypes.STAND_ATTACK);
				DamageSource dmgSource = new DamageSource(damageType, performer);
				((RipplesModifiedDamageSource) dmgSource).jojo_ripples$modifyKnockback(0, 0.1f);
				float dmgAmount = StandStatFormulas.getBarrageHitDamage(stand.getAttackDamage()) * hitsThisTick;
				standEntityAttack(stand, targetLiving, dmgSource, dmgAmount);
				
				stand.addFinisherMeter(0.005f * hitsThisTick);
			}
		}
		
		protected void mineBlock(ActionTarget blockTarget, Level level, StandEntity stand) {
			BlockPos blockPos = blockTarget.getBlockPos();
			BlockState blockState = level.getBlockState(blockPos);
			
			double standStrength = stand.getAttackDamage();
			double standSpeed = stand.getAttackSpeed();
			
			float blockHardnessForStand = StandStatFormulas.getBlockHardness(standStrength, blockState, level, blockPos);
			if (blockHardnessForStand >= 0) {
				float standEfficiency = StandStatFormulas.getBarrageBlockMiningEfficiency(standStrength, standSpeed);
				float destroyProgress = standEfficiency / blockHardnessForStand;
				
				boolean brokenBlock = ServerBlockDestroyTracker.addBlockDestroyProgress((ServerLevel) level, stand, blockPos, destroyProgress);
				if (brokenBlock) {
					boolean dropBlock = !isUserCreative();
					level.destroyBlock(blockPos, dropBlock, stand);
					return;
				}
			}
			
			if (curPhaseTick % 2 == 0) {
				SoundType blockSounds = blockState.getSoundType(level, blockPos, stand);
				level.playSound(null, blockPos, blockSounds.getHitSound(), SoundSource.BLOCKS, 
						(blockSounds.getVolume() + 1.0F) / 8.0F, blockSounds.getPitch() * 0.5F);
			}
		}
		
	}

}
