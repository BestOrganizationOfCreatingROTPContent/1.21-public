package com.github.standobyte.jojo.powersystem.entityaction;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId.AbilityInputNetwork;

import net.minecraft.network.RegistryFriendlyByteBuf;
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


	@ApiStatus.Internal
	default void encodeAbility(RegistryFriendlyByteBuf buffer) {
		AbilityInputNetwork.encodeInput(buffer, (Ability) this, null);
	}

	public static EntityActionInstance decodeAbilityAction(RegistryFriendlyByteBuf buffer) {
		Ability ability = AbilityInputNetwork.decodeInput(buffer).getAbility(null);
		EntityActionType actionSupplier = ability instanceof EntityActionType entityAbility ? entityAbility : null;
		return actionSupplier != null ? actionSupplier.createActionObj() : null;
	}


	/**
	 * A helper method to create the action to be set to the performer when they user uses an ability (e.g. a Hamon attack).
	 * @param level The method is called both on server and on the player user's client.
	 * @param user The player/mob user.
	 * @return The instance of the action, to be able to stop it when the player stops holding the button.
	 */
	default EntityActionInstance initActionOnAbilityUse(Level level, LivingEntity user) {
		EntityActionInstance action = createActionObj();
		initActionFromConfig(action, level, user);
		action.setPhaseZero();
		return action;
	}

	void initActionFromConfig(EntityActionInstance action, Level level, LivingEntity user);


	default LivingEntity getPerformer(LivingEntity user) {
		return user;
	}

	ActionAnimIdentifier getEntityAnim(EntityActionInstance action);
}
