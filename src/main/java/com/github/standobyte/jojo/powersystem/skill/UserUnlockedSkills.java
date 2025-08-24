package com.github.standobyte.jojo.powersystem.skill;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class UserUnlockedSkills {
	protected Map<ResourceLocation, Set<String>> unlocked = new HashMap<>();
	
	public boolean isUnlocked(ResourceLocation powerType, String skillName) {
		Set<String> skillsOfThisPowerType = unlocked.get(powerType);
		return skillsOfThisPowerType != null && skillsOfThisPowerType.contains(skillName);
	}
	
	public boolean setUnlocked(ResourceLocation powerType, String skillName, boolean unlocked) {
		if (unlocked) {
			return this.unlocked.computeIfAbsent(powerType, __ -> new HashSet<>()).add(skillName);
		}
		else {
			Set<String> skillsOfThisPowerType = this.unlocked.get(powerType);
			return skillsOfThisPowerType != null && skillsOfThisPowerType.remove(skillName);
		}
	}
	

	// TODO (skill unlocking) sync to user
	public void write(FriendlyByteBuf buf)  {
		
	}
	
	public void read(FriendlyByteBuf buf) {
		
	}

	// TODO (skill unlocking) save in nbt
	public CompoundTag toNBT() {
		CompoundTag nbt = new CompoundTag();
		
		return nbt;
	}
	
	public void fromNBT(CompoundTag nbt) {
		
	}
}
