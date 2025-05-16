package com.github.standobyte.jojo.powersystem.ability;

import java.util.concurrent.atomic.AtomicInteger;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.core.packet.fromserver.TrAbilityUsePacket;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInputState;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInputState.HeldInputContainer;
import com.github.standobyte.jojo.powersystem.entityaction.HeldInput;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

public class AbilityInput {

	@ApiStatus.Internal
	public static void click(Ability ability, LivingEntity user, RegistryFriendlyByteBuf extraData, float timeTookToResolve) {
		if (ability == null || user == null) return;

		ability = Ability.resolveSubAbility(ability, user);
		Level level = user.level();
		ability.onClick(level, user);
		if (!level.isClientSide()) {
			PacketDistributor.sendToPlayersTrackingEntity(user, 
					TrAbilityUsePacket.click(user.getId(), ability, timeTookToResolve));
		}
	}

	/* 
	 * FIXME ability inputs that are currently controlled purely by the client
	 * 	barrage can refresh its duration via a client-sent packet
	 * 	the client can use abilities from other movesets
	 */
	@ApiStatus.Internal
	public static void startHolding(short keyId, Ability ability, 
			LivingEntity user, RegistryFriendlyByteBuf extraData, float timeTookToResolve) {
		if (ability == null || user == null) return;

		ability = Ability.resolveSubAbility(ability, user);
		EntityActionInputState inputHandler = user.getData(ModDataAttachmentTypes.ENTITY_ABILITY_INPUT.get());
		if (inputHandler != null) {
			Level level = user.level();
			HeldInput action = ability.onButtonStartHold(level, user);
			if (!level.isClientSide()) {
				PacketDistributor.sendToPlayersTrackingEntity(user, 
						TrAbilityUsePacket.startHold(user.getId(), keyId, ability, timeTookToResolve));
			}
			
			inputHandler.heldKeys.put(keyId, new HeldInputContainer(ability, action));
		}
	}

	@ApiStatus.Internal
	public static void releaseHolding(short keyId, LivingEntity user) {
		EntityActionInputState inputHandler = user.getData(ModDataAttachmentTypes.ENTITY_ABILITY_INPUT.get());
		if (inputHandler != null) {
			HeldInputContainer heldAction = inputHandler.heldKeys.remove(keyId);
			if (heldAction != null) {
				HeldInput action = heldAction.action;
				Level level = user.level();

				if (action != null) {
					action.onStopHeld(user);
				}
				if (!level.isClientSide()) {
					PacketDistributor.sendToPlayersTrackingEntity(user, 
							TrAbilityUsePacket.releaseHold(user.getId(), keyId));
				}
			}
		}
	}
	
	
	private static final AtomicInteger pseudoKey = new AtomicInteger();
	public static short makeMobFakeKeyId(Ability ability) {
		pseudoKey.incrementAndGet();
		return pseudoKey.shortValue();
	}


	public enum InputEventType {
		PRESS_CLICK,
		PRESS_HOLD,
		RELEASE
	}
	
	public enum InputType {
		CLICK,
		HOLD
	}

}
