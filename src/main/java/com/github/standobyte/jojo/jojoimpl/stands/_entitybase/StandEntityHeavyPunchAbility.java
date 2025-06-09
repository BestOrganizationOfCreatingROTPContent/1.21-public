package com.github.standobyte.jojo.jojoimpl.stands._entitybase;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.sound.ClientsideSoundsHelper;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.ModSoundEvents;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandOffsetFromUser;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class StandEntityHeavyPunchAbility extends StandEntityAbility {

	public StandEntityHeavyPunchAbility(AbilityId abilityId) {
		super(abilityId);
		setDefaultPhaseLength(ActionPhase.WINDUP, 8);
		setDefaultPhaseLength(ActionPhase.PERFORM, 4);
		setDefaultPhaseLength(ActionPhase.RECOVERY, 20);
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
		public void actionPerformEnd() {
			JojoMod.LOGGER.debug("ORAAAAA");
			
			if (standAimTarget != null) {
				Entity aimTargetEntity = standAimTarget.getEntity();
				if (aimTargetEntity == null || aimTargetEntity.isRemoved()) {
					// TODO clear the target if it's too far away / outside of the *stand's* vision
					standAimTarget = null;
				}
			}
		}
		
	}

}
