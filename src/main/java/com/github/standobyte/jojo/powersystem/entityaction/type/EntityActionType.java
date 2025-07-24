package com.github.standobyte.jojo.powersystem.entityaction.type;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId.AbilityInputNetwork;
import com.github.standobyte.jojo.powersystem.entityaction.ActionAnimIdentifier;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPower;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public interface EntityActionType {
	/**
	 * Is used for synchronization as well, so it only needs to create the object itself.
	 */
	@ApiStatus.OverrideOnly
	default EntityActionInstance createActionObj() {
		return new EntityActionInstance(this);
	}


	/**
	 * A helper method to create the action to be set to the performer when they user uses an ability (e.g. a Hamon attack).
	 * @param level The method is called both on server and on the player user's client.
	 * @param user The player/mob user.
	 * @param extraInput The extra input needed for some abilities to work, defined by overriding {@link Ability#writeExtraInput(FriendlyByteBuf, LivingEntity)}
	 * @return The instance of the action, to be able to stop it when the player stops holding the button.
	 */
	default EntityActionInstance initActionOnAbilityUse(Level level, LivingEntity user, @Nullable FriendlyByteBuf extraInput) {
		EntityActionInstance action = createActionObj();
		initActionFromConfig(action, level, user);
		action.start();
		if (extraInput != null) {
			action.extraClientInput(extraInput);
		}
		return action;
	}

	@ApiStatus.OverrideOnly
	default void initActionFromConfig(EntityActionInstance action, Level level, LivingEntity user) {
		for (ActionPhase phase : ActionPhase.values()) {
			action.phasesLength.put(phase, phase == ActionPhase.PERFORM ? 1f : 0f);
		}
	}


	@ApiStatus.Internal
	default void encodeAbility(RegistryFriendlyByteBuf buffer) {
		buffer.writeBoolean(true);
		AbilityInputNetwork.encodeInput(buffer, (Ability) this, null);
	}

	@ApiStatus.Internal
	public static EntityActionInstance decodeAbilityAction(RegistryFriendlyByteBuf buffer) {
		boolean isPowerMovesetAbility = buffer.readBoolean();
		EntityActionType actionType;
		if (isPowerMovesetAbility) {
			Ability ability = AbilityInputNetwork.decodeInput(buffer).getAbility(null);
			actionType = ability instanceof EntityActionType entityAbility ? entityAbility : null;
		}
		else {
			ResourceLocation specialActionId = buffer.readResourceLocation();
			actionType = JojoRegistries.NON_POWER_ACTIONS_REG.getValue(specialActionId);
		}
		return actionType != null ? actionType.createActionObj() : null;
	}


	@ApiStatus.OverrideOnly
	default boolean shouldBufferInput(LivingComponentAction performerAction) {
		EntityActionInstance curAction = performerAction.getAction();
		return curAction != null && !curAction.canBeCancelledInto(this);
	}
	
	
	default ResourceLocation getEntityAnimSet(LivingEntity user) {
		PlayerPower power = PlayerPower.get(user);
		if (power != null && power.hasPower()) {
			return power.getPowerType().getId();
		}
		return null;
	}

	ActionAnimIdentifier getEntityAnim(EntityActionInstance action);
}
