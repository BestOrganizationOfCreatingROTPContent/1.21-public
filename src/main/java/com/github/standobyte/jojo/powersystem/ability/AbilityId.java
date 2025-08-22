package com.github.standobyte.jojo.powersystem.ability;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.PowerType;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public record AbilityId(PowerClass<?> powerClass, ResourceLocation powerTypeId, String nameInMoveset) {

	public static Ability getAbility(PowerClass<?> powerClass, Power<?> userPower, ResourceLocation powerTypeId, String nameInMoveset) {
		if (powerClass == null || powerTypeId == null) return null;
		PowerType powerType = powerClass.getPowerType(powerTypeId);
		return powerType != null ? userPower.getMoveset().getAbility(nameInMoveset) : null;
	}
	
	
	static <A extends Ability> A makeDefaultAbilityInstance(AbilityType<A> abilityType) {
		return abilityType.createInstance(new AbilityId(null, null, abilityType.registryKey.toString()));
	}


	public static class AbilityInputNetwork {
		private final SyncStrategy syncStrategy;
		private final PowerClass<?> powerClass;
		private final String abilityName;

		AbilityInputNetwork(SyncStrategy syncStrategy, PowerClass<?> powerClass, String abilityName) {
			this.syncStrategy = syncStrategy;
			this.powerClass = powerClass;
			this.abilityName = abilityName;
		}


		public static void encodeInput(FriendlyByteBuf buffer, @Nullable Ability ability, @Nullable Power<?> userPower) {
			encodeInput(buffer, ability != null ? ability.abilityId : null, userPower);
		}

		public static void encodeInput(FriendlyByteBuf buffer, @Nullable AbilityId abilityId, @Nullable Power<?> userPower) {
			if (abilityId == null) {
				buffer.writeEnum(SyncStrategy.NULL_ABILITY);
				return;
			}
			
			SyncStrategy strategy;
			if (abilityId.powerTypeId == null) {
				strategy = SyncStrategy.DEFAULT_ABILITY_INSTANCE;
			}
			else {
				strategy = SyncStrategy.FROM_PLAYER_MOVESET;
			}
			buffer.writeEnum(strategy);
			
			switch (strategy) {
				case FROM_PLAYER_MOVESET -> {
					PowerClass.NETWORK_CODEC.encode(buffer, abilityId.powerClass());
					buffer.writeUtf(abilityId.nameInMoveset());
				}
				case DEFAULT_ABILITY_INSTANCE -> {
					buffer.writeUtf(abilityId.nameInMoveset());
				}
				default -> {}
			}
		}


		public static AbilityInputNetwork decodeInput(FriendlyByteBuf buffer) {
			SyncStrategy strategy = buffer.readEnum(SyncStrategy.class);
			return switch (strategy) {
				case NULL_ABILITY -> new AbilityInputNetwork(SyncStrategy.NULL_ABILITY, null, null);
				case FROM_PLAYER_MOVESET -> {
					PowerClass<?> powerClass = PowerClass.NETWORK_CODEC.decode(buffer);
					String abilityName = buffer.readUtf();
					
					yield new AbilityInputNetwork(SyncStrategy.FROM_PLAYER_MOVESET, powerClass, abilityName);
				}
				case DEFAULT_ABILITY_INSTANCE -> {
					String abilityName = buffer.readUtf();
					
					yield new AbilityInputNetwork(SyncStrategy.DEFAULT_ABILITY_INSTANCE, null, abilityName);
				}
			};
		}
		
		public Ability getAbility(LivingEntity powerUser) {
			Ability ability;
			return switch (syncStrategy) {
				case NULL_ABILITY -> null;
				case FROM_PLAYER_MOVESET -> {
					if (powerUser == null) throw new IllegalStateException("Failed to sync ability " + abilityName + " (needs user entity)");
					Power<?> userPower = powerClass.get(powerUser);
					if (userPower == null) throw new IllegalStateException("Failed to sync ability " + abilityName + " (user data not attached)");
					
					ability = userPower.getAbility(abilityName);
					if (ability == null) {
						LivingEntity user = userPower.getUser();
						if (userPower.hasPower()) throw new IllegalStateException("Failed to sync ability " + abilityName + " (ability not found in the moveset " + userPower.getPowerType().getId() + " of " + user.getName().getString() + ")");
						else throw new IllegalStateException("Failed to sync ability " + abilityName + " (" + user.getName().getString() + " has no moveset of " + userPower.getClass() + " class)");
					}
					
					yield ability;
				}
				case DEFAULT_ABILITY_INSTANCE -> {
					ResourceLocation abilityTypeId = ResourceLocation.parse(abilityName);
					AbilityType<?> abilityType = JojoRegistries.ABILITY_TYPES_REG.get(abilityTypeId);
					
					yield abilityType != null ? abilityType._defaultAbilityInstance : null;
				}
			};
		}
		
		enum SyncStrategy {
			NULL_ABILITY,
			FROM_PLAYER_MOVESET,
			DEFAULT_ABILITY_INSTANCE;
		}
	}
	
}
