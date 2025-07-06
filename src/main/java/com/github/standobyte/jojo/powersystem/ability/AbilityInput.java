package com.github.standobyte.jojo.powersystem.ability;

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.packet.fromserver.TrAbilityUsePacket;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInputState;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInputState.HeldInputEntry;
import com.github.standobyte.jojo.powersystem.entityaction.HeldInput;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

public class AbilityInput {

	public static void click(Ability ability, LivingEntity user, FriendlyByteBuf extraClientInput, float clickHoldResolveTime) {
		if (ability == null || user == null) return;

		Level level = user.level();
		ability.onClick(level, user, extraClientInput, clickHoldResolveTime);
		if (!level.isClientSide()) {
			PacketDistributor.sendToPlayersTrackingEntity(user, 
					TrAbilityUsePacket.click(user.getId(), ability, clickHoldResolveTime, user));
		}
	}

	public static void clickMob(Ability ability, LivingEntity user, FriendlyByteBuf extraData) {
		click(ability, user, extraData, 0);
	}

	/* 
	 * FIXME ability inputs that are currently controlled purely by the client
	 * 	barrage can refresh its duration via a client-sent packet
	 * 	the client can use abilities from other movesets
	 */
	@Nullable
	public static HeldInputEntry startHolding(short keyId, Ability ability, 
			LivingEntity user, FriendlyByteBuf extraClientInput, float clickHoldResolveTime) {
		if (ability == null || user == null) return null;

		EntityActionInputState inputHandler = user.getData(ModDataAttachmentTypes.ENTITY_ABILITY_INPUT.get());
		if (inputHandler == null) return null;
		
		Level level = user.level();
		HeldInput action = ability.onButtonStartHold(level, user, extraClientInput, clickHoldResolveTime);
		if (!level.isClientSide()) {
			PacketDistributor.sendToPlayersTrackingEntity(user, 
					TrAbilityUsePacket.startHold(user.getId(), keyId, ability, clickHoldResolveTime, user));
		}
		
		HeldInputEntry heldInput = new HeldInputEntry(keyId, ability, action);
		inputHandler.heldKeys.put(keyId, heldInput);
		return heldInput;
	}
	
	public static HeldInputEntry startHoldingMob(Ability ability, LivingEntity user, FriendlyByteBuf extraData) {
		short keyId = (short) pseudoKey.incrementAndGet();
		return startHolding(keyId, ability, user, extraData, 0);
	}
	private static final AtomicInteger pseudoKey = new AtomicInteger();

	public static void releaseHolding(short keyId, LivingEntity user) {
		EntityActionInputState inputHandler = user.getData(ModDataAttachmentTypes.ENTITY_ABILITY_INPUT.get());
		if (inputHandler != null) {
			HeldInputEntry heldAction = inputHandler.heldKeys.remove(keyId);
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
