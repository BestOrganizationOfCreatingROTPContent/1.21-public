package com.github.standobyte.jojo.jojoimpl.stands._entitybase;

import java.util.ArrayList;
import java.util.List;

import com.github.standobyte.jojo.core.JojoMod;
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

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class StandEntityPunchAbility extends StandEntityAbility {
	public List<String> punchNames;

	public StandEntityPunchAbility(AbilityId abilityId) {
		super(abilityId);
		setDefaultPhaseLength(ActionPhase.WINDUP, 4);
		setDefaultPhaseLength(ActionPhase.PERFORM, 2);
		setDefaultPhaseLength(ActionPhase.RECOVERY, 10);
		punchNames = new ArrayList<>();
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

		public StandEntityPunch(EntityActionType ability) {
			super(ability);
		}
		
		@Override
		public void onActionSet() {
			if (performer instanceof StandEntity standEntity) {
				LivingEntity user = getPowerUser();
				if (user != null) standEntity.offsetFromUser.setOffset(
						new Vec3(0, StandEntity.Y_OFFSET, 1), StandOffsetFromUser.OffsetMode.HEAD, user);
			}
		}
		
		@Override
		public void actionPerform() {
			JojoMod.LOGGER.debug("ORA {}", ((Ability) ability).abilityId.nameInMoveset());
		}
		
	}

}
