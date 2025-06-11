package com.github.standobyte.jojo.jojoimpl.stands._entitybase;

import java.util.ArrayList;
import java.util.List;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.sound.ClientsideSoundsHelper;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.ModSoundEvents;
import com.github.standobyte.jojo.powersystem.Moveset;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandOffsetFromUser;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class StandEntityPunchAbility extends StandEntityAbility {
	public List<String> punchNames;

	public StandEntityPunchAbility(AbilityId abilityId) {
		super(abilityId);
		setDefaultPhaseLength(ActionPhase.WINDUP, 4);
		setDefaultPhaseLength(ActionPhase.PERFORM, 2);
		setDefaultPhaseLength(ActionPhase.RECOVERY, 20);
		punchNames = new ArrayList<>();
		punchNames.add(this.abilityId.nameInMoveset());
	}
	
	@Override
	public Ability replaceWithSubAbility(LivingEntity user) {
		StandPower standPower = PowerClass.STAND.get(user);
		if (standPower != null) {
			Moveset moveset = standPower.getMoveset();
			int startFromPunch = 0;
			
			StandEntity standEntity = standPower.getSummonedStandEntity();
			if (standEntity != null) {
				EntityActionInstance standAction = standEntity.getCurStandAction();
				if (standAction != null) {
					String actionName = ((Ability) standAction.ability).abilityId.nameInMoveset();
					for (int i = 0; i < punchNames.size(); i++) {
						if (punchNames.get(i).equals(actionName)) {
							startFromPunch = i + 1;
							break;
						}
					}
				}
			}
			
			int size = punchNames.size();
			for (int i = 0; i < size; i++) {
				int index = (startFromPunch + i) % size;
				String nextPunchName = punchNames.get(index);
				Ability nextPunch = moveset.getAbility(nextPunchName);
				if (nextPunch != null) {
					return nextPunch;
				}
			}
		}
		
		return super.replaceWithSubAbility(user);
	}
	
	
	@Override
	public EntityActionInstance createActionObj() {
		return new StandEntityPunch(this);
	}
	
	public static class StandEntityPunch extends EntityActionInstance {
		protected boolean playedSwingSound;
		protected boolean playedStandCrySound;

		public StandEntityPunch(EntityActionType ability) {
			super(ability);
		}
		
		@Override
		public void onActionSet(EntityActionInstance prevAction) {
			playedStandCrySound = prevAction != null;
			setStandOffset(0, 2, StandOffsetFromUser.OffsetMode.HEAD_XY, false);
			keepStandAimedAtTarget();
		}
		
		@Override
		public void actionTick() {
			Level level = performer.level();
			if (level.isClientSide() && ClientGlobals.canHearStands && !(playedSwingSound && playedStandCrySound) && performer instanceof StandEntity stand) {
				if (!playedSwingSound) {
					// how many ticks are left before the start of the 'perform' phase (when actionPerformStart() is called)
					int ticksDiff = (int) (calcFullTicks(ActionPhase.PERFORM, 0) - getFullTicksPassed());
					if (ticksDiff <= 2) {
						level.playLocalSound(stand.getX(), stand.getEyeY(), stand.getZ(), ClientsideSoundsHelper.withStandSkin(
								ModSoundEvents.STAND_PUNCH_SWING.get(), stand.getStandId(), stand.getStandSkin()), 
								stand.getSoundSource(), 1, 1, false);
						playedSwingSound = true;
					}
				}
				
				if (!playedStandCrySound) {
					ClientsideSoundsHelper.playEntityLingeringSound(stand, ClientsideSoundsHelper.withStandSkin(
							ModSoundEvents.STAND_PUNCH_CRY.get(), stand.getStandId(), stand.getStandSkin()), 
							stand.getSoundSource(), 1, 1, level);
					playedStandCrySound = true;
				}
			}
		}
		
		@Override
		public void actionPerformStart() {
			JojoMod.LOGGER.debug("ORA {}", ((Ability) ability).abilityId.nameInMoveset());
		}
		
		@Override
		public void actionPerformEnd() {
			if (standAimTarget != null) {
				Entity aimTargetEntity = standAimTarget.getEntity();
				if (aimTargetEntity == null || aimTargetEntity.isRemoved()) {
					// TODO clear the target if it's too far away / outside of the *stand's* vision
					standAimTarget = null;
				}
			}
		}
		
	}
	
	
	public static boolean canStandHit(StandEntity stand, Entity target) {
		return EntitySelector.CAN_BE_PICKED.test(target) && stand.canAttackEntity(target);
	}

}
