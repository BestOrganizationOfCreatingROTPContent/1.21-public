package com.github.standobyte.jojo.powersystem.standpower;

import com.github.standobyte.jojo.powersystem.skill.UnlockableSkill;

public class StandUnlockableSkill extends UnlockableSkill {
	public int pointsToUnlock;

	public StandUnlockableSkill(String name) {
		super(name);
	}
	
	public StandUnlockableSkill setPointsToUnlock(int skillPoints) {
		this.pointsToUnlock = skillPoints;
		return this;
	}
	
	public StandUnlockableSkill setIsStartingSkill() {
		setPointsToUnlock(0);
		return this;
	}
	
	public static StandUnlockableSkill unlockableAbility(String name, int skillPoints) {
		StandUnlockableSkill skill = new StandUnlockableSkill(name);
		skill.withAbility(name);
		skill.setPointsToUnlock(skillPoints);
		return skill;
	}
	
	public static StandUnlockableSkill startingAbility(String name) {
		StandUnlockableSkill skill = new StandUnlockableSkill(name);
		skill.withAbility(name);
		skill.setIsStartingSkill();
		return skill;
	}

}
