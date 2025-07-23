package com.github.standobyte.jojo.jojoimpl.stands._entitybase;

import com.github.standobyte.jojo.client.input.AbilityInputState;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;
import com.github.standobyte.jojo.util.StandUtil;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.Tags;

public class StandBearingShotAbility extends StandEntityAbility {

	public StandBearingShotAbility(AbilityId abilityId) {
		super(abilityId);
		setDefaultPhaseLength(ActionPhase.PERFORM, 999999);
	}
	
	@Override
	public boolean isAbilityAvailable(Power<?> context) {
		if (super.isAbilityAvailable(context)) {
			StandEntity standEntity = StandUtil.getSummonedStand(context);
			if (standEntity != null) {
				ItemStack lItem = standEntity.getOffhandItem();
				ItemStack rItem = standEntity.getMainHandItem();
				return lItem.is(Tags.Items.NUGGETS) || rItem.is(Tags.Items.NUGGETS);
			}
		}
		return false;
	}
	
	@Override
	public AbilityInputState cl_abilityInputState(Power<?> context) {
		AbilityInputState state = super.cl_abilityInputState(context);
		state.setFlag(AbilityInputState.WITH_ITEM_HELD, true);
		return state;
	}
	
	
	@Override
	public EntityActionInstance createActionObj() {
		return new StandBearingShot(this);
	}
	
	public static class StandBearingShot extends EntityActionInstance {

		public StandBearingShot(EntityActionType ability) {
			super(ability);
		}

		@Override
		public void onButtonStopHold() {
			if (getPhase() != ActionPhase.RECOVERY) {
				startPhase(ActionPhase.RECOVERY);
				syncPhaseChanges();
				
				JojoMod.LOGGER.debug("pew");
			}
		}

		@Override
		public boolean canBeCancelledInto(EntityActionType cancellingAbility) {
			return true;
		}
		
	}
}
