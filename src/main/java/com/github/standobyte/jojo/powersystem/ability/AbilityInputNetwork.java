package com.github.standobyte.jojo.powersystem.ability;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class AbilityInputNetwork {
	// true - get the ability directly from the specific PowerType moveset, 
	// false - use the moveset from the user's Power object
	private final boolean fullId;
	private final PowerClass<?> powerClass;
	private final ResourceLocation powerTypeId;
	private final String abilityName;

	public static void encodeInput(FriendlyByteBuf buffer, @Nullable Ability<?> ability, @Nullable Power<?> userPower) {
		if (ability == null) {
			buffer.writeByte(0);
			return;
		}
		String abilityName = ability.abilityId.nameInMoveset();
		boolean sendFullId = !(userPower != null && userPower.hasPower() && 
				userPower.getPowerClass() == ability.getPowerClass() && 
				userPower.getAbility(abilityName) == ability);
		buffer.writeByte(sendFullId ? 3 : 1);
		if (sendFullId) {
			PowerClass.NETWORK_CODEC.encode(buffer, ability.getPowerClass());
			buffer.writeResourceLocation(ability.abilityId.powerTypeId());
		}
		buffer.writeUtf(abilityName);
	}

	public static AbilityInputNetwork decodeInput(FriendlyByteBuf buffer) {
		byte state = buffer.readByte();
		if (state == 0) {
			return null;
		}
		boolean sentFullId = (state & 2) > 0;
		String abilityName;
		if (sentFullId) {
			PowerClass<?> powerClass = PowerClass.NETWORK_CODEC.decode(buffer);
			ResourceLocation powerTypeId = buffer.readResourceLocation();
			abilityName = buffer.readUtf();
			return new AbilityInputNetwork(powerClass, powerTypeId, abilityName);
		}
		else {
			abilityName = buffer.readUtf();
			return new AbilityInputNetwork(abilityName);
		}
	}
	
	AbilityInputNetwork(PowerClass<?> powerClass, ResourceLocation powerTypeId, String abilityName) {
		this.fullId = true;
		this.powerClass = powerClass;
		this.powerTypeId = powerTypeId;
		this.abilityName = abilityName;
	}
	
	AbilityInputNetwork(String abilityName) {
		this.fullId = false;
		this.powerClass = null;
		this.powerTypeId = null;
		this.abilityName = abilityName;
	}
	
	public Ability<?> getAbility(Power<?> userPower) {
		Ability<?> ability;
		if (fullId) {
			ability = powerClass.getAbility(powerTypeId, abilityName);
			if (ability == null) throw new IllegalStateException("Failed to use ability " + abilityName + " (ability not found in the moveset " + powerTypeId + ")");
			return ability;
		}
		else {
			if (userPower == null) throw new IllegalStateException("Failed to use ability " + abilityName + " (user data not attached)");
			ability = userPower.getAbility(abilityName);
			if (ability == null) {
				LivingEntity user = userPower.getUser();
				if (userPower.hasPower()) throw new IllegalStateException("Failed to use ability " + abilityName + " (ability not found in the moveset " + userPower.getPowerType().getId() + " of " + user.getName().getString() + ")");
				else throw new IllegalStateException("Failed to use ability " + abilityName + " (" + user.getName().getString() + " has no moveset of " + userPower.getClass() + " class)");
			}
			return ability;
		}
	}
}
