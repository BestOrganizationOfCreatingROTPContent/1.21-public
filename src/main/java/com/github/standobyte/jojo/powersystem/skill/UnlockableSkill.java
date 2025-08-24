package com.github.standobyte.jojo.powersystem.skill;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.chat.Component;

public abstract class UnlockableSkill {
	public final String skillName;
	public List<String> prerequisiteSkills;
	public List<String> unlocksAbilities;
	
	public Component textName;
	public Component textDesc;
	public Component textControls;

	public UnlockableSkill(String name) {
		this.skillName = name;
		this.prerequisiteSkills = new ArrayList<>();
		this.unlocksAbilities = new ArrayList<>();
		this.textName = Component.translatable("jojo_ripples.skill." + name);
		this.textDesc = Component.translatable("jojo_ripples.skill." + name + ".desc");
		this.textControls = Component.translatable("jojo_ripples.skill." + name + ".controls");
	}
	
	public UnlockableSkill withAbility(String abilityName) {
		this.unlocksAbilities.add(abilityName);
		return this;
	}
	
	public UnlockableSkill prerequisiteSkill(String name) {
		this.prerequisiteSkills.add(name);
		return this;
	}
	
}
