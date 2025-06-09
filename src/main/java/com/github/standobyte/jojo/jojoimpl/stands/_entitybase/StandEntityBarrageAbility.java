package com.github.standobyte.jojo.jojoimpl.stands._entitybase;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.sound.ClientsideSoundsHelper;
import com.github.standobyte.jojo.client.sound.sounds.EntityStoppableSoundInstance;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.ModSoundEvents;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandOffsetFromUser;

import net.minecraft.world.level.Level;

public class StandEntityBarrageAbility extends StandEntityAbility {

	public StandEntityBarrageAbility(AbilityId abilityId) {
		super(abilityId);
		setDefaultPhaseLength(ActionPhase.PERFORM, 100);
		setDefaultPhaseLength(ActionPhase.RECOVERY, 10);
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
			setStandOffset(0, 1.5, StandOffsetFromUser.OffsetMode.HEAD_XY, true);
			Level level = performer.level();
			if (level.isClientSide()) {
				if (performer instanceof StandEntity stand) {
					EntityStoppableSoundInstance sound = new EntityStoppableSoundInstance(ClientsideSoundsHelper.withStandSkin(
							ModSoundEvents.STAND_BARRAGE_CRY.get(), stand.getStandId(), stand.getStandSkin()), 
							stand.getSoundSource(), 1, 1, stand, level.random.nextLong(), () -> this.phase != ActionPhase.PERFORM);
					ClientsideSoundsHelper.playNonVanillaClassSound(sound);
				}
			}
		}

		@Override
		public void actionTick() {
			if (getPhase() == ActionPhase.PERFORM) {
				Level level = performer.level();
				if (level.isClientSide()) {
					if (ClientGlobals.canHearStands && performer instanceof StandEntity stand) {
						level.playLocalSound(stand.getX(), stand.getEyeY(), stand.getZ(), ClientsideSoundsHelper.withStandSkin(
								ModSoundEvents.STAND_PUNCH_BARRAGE_SWING.get(), stand.getStandId(), stand.getStandSkin()), 
								stand.getSoundSource(), 1, 1, false);
					}
				}
				else {
					
					JojoMod.LOGGER.debug("ORAORAORA");
					
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
		
	}

}
