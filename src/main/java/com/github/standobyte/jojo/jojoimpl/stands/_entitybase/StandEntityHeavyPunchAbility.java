package com.github.standobyte.jojo.jojoimpl.stands._entitybase;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.packet.fromserver.StandEntitySoundPacket;
import com.github.standobyte.jojo.init.ModSoundEvents;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandOffsetFromUser;

import net.neoforged.neoforge.network.PacketDistributor;

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

		public StandEntityHeavyPunch(EntityActionType ability) {
			super(ability);
		}
		
		@Override
		public void onActionSet() {
			setStandOffset(0, 2, StandOffsetFromUser.OffsetMode.HEAD_XY, false);
		}
		
		@Override
		public void actionTick() {
			if (!performer.level().isClientSide()) {
				// play the swing sound 5 ticks before the end of the 'perform' phase (when actionPerformEnd() is called)
				if (soundTiming(this, ActionPhase.PERFORM, phasesLength.get(ActionPhase.PERFORM), -5) && performer instanceof StandEntity stand) {
					PacketDistributor.sendToPlayersTrackingEntityAndSelf(stand, new StandEntitySoundPacket(stand, ModSoundEvents.STAND_PUNCH_HEAVY_SWING, 1, 1));
				}
			}
		}

		@Override
		public void actionPerformEnd() {
			JojoMod.LOGGER.debug("ORAAAAA");
		}
	}

}
