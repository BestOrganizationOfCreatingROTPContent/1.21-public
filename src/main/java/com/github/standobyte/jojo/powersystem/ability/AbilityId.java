package com.github.standobyte.jojo.powersystem.ability;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.PowerType;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public record AbilityId(PowerClass<?> powerClass, ResourceLocation powerTypeId, String nameInMoveset) {

	public static Ability getAbility(PowerClass<?> powerClass, ResourceLocation powerTypeId, String nameInMoveset) {
		if (powerClass == null || powerTypeId == null) return null;
		PowerType powerType = powerClass.getPowerType(powerTypeId);
		return powerType != null ? powerType.getMoveset().getAbility(nameInMoveset) : null;
	}
	
	public boolean canJustSendAbilityName(Power<?> power) {
		return power != null && power.getPowerClass() == this.powerClass
				&& power.hasPower() && power.getPowerType().getId().equals(this.powerTypeId);
	}


	public static class AbilityInputNetwork {
		private final String abilityName;
		// true - get the ability directly from the specific PowerType moveset, 
		// false - use the moveset from the user's Power object
		private final boolean fullId;
		private final PowerClass<?> powerClass;
		private final ResourceLocation powerTypeId;

		public static void encodeInput(FriendlyByteBuf buffer, @Nullable Ability ability, @Nullable Power<?> userPower) {
			encodeInput(buffer, ability != null ? ability.abilityId : null, userPower);
		}

		public static void encodeInput(FriendlyByteBuf buffer, @Nullable AbilityId abilityId, @Nullable Power<?> userPower) {
			if (abilityId == null) {
				buffer.writeByte(0);
				return;
			}
			
			boolean sendFullId = !abilityId.canJustSendAbilityName(userPower);
			buffer.writeByte(sendFullId ? 3 : 1);
			
			PowerClass.NETWORK_CODEC.encode(buffer, abilityId.powerClass());
			buffer.writeUtf(abilityId.nameInMoveset());
			
			if (sendFullId) {
				buffer.writeResourceLocation(abilityId.powerTypeId());
			}
		}

		public static AbilityInputNetwork decodeInput(FriendlyByteBuf buffer) {
			byte state = buffer.readByte();
			if (state == 0) {
				return null;
			}
			
			boolean sentFullId = (state & 2) > 0;
			
			PowerClass<?> powerClass = PowerClass.NETWORK_CODEC.decode(buffer);
			String abilityName = buffer.readUtf();
			
			if (sentFullId) {
				ResourceLocation powerTypeId = buffer.readResourceLocation();
				return new AbilityInputNetwork(powerClass, powerTypeId, abilityName);
			}
			else {
				return new AbilityInputNetwork(powerClass, abilityName);
			}
		}
		
		AbilityInputNetwork(PowerClass<?> powerClass, ResourceLocation powerTypeId, String abilityName) {
			this.fullId = true;
			this.powerClass = powerClass;
			this.powerTypeId = powerTypeId;
			this.abilityName = abilityName;
		}
		
		AbilityInputNetwork(PowerClass<?> powerClass, String abilityName) {
			this.fullId = false;
			this.powerClass = powerClass;
			this.powerTypeId = null;
			this.abilityName = abilityName;
		}
		
		public Ability getAbility(LivingEntity powerUser) {
			Ability ability;
			if (fullId) {
				ability = AbilityId.getAbility(powerClass, powerTypeId, abilityName);
				if (ability == null) throw new IllegalStateException("Failed to use ability " + abilityName + " (ability not found in the moveset " + powerTypeId + ")");
				return ability;
			}
			else {
				Power<?> userPower = powerClass.get(powerUser);
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
	
}
