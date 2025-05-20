package com.github.standobyte.jojo.powersystem.entityaction;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityInput.InputType;
import com.github.standobyte.jojo.powersystem.entityaction.netcode.SyncType;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.util.entitycomponent.TickingEntityData;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.entity.LivingEntity;

@ApiStatus.Internal
public class EntityActionInputState implements TickingEntityData {
	protected final LivingEntity user;

	public EntityActionInputState(LivingEntity entity) {
		this.user = entity;
		addTicking(entity);
	}

	@Override
	public void tick() {
		tickInputBuffer();
	}

	// Held keys stuff below

	// TODO (entity action 2) if the player logs out and the action gets saved in NBT, after relog they won't be able to stop the action - fix that
	@ApiStatus.Internal
	public final Int2ObjectMap<HeldInputEntry> heldKeys = new Int2ObjectArrayMap<>();

	public static class HeldInputEntry {
		public final short keyId;
		@Nullable public HeldInput action;

		public HeldInputEntry(short keyId, Ability ability, HeldInput action) {
			this.keyId = keyId;
			this.action = action;
		}
	}

	// Input buffer stuff below

	protected Map<LivingEntity, BufferedInputEntry> bufferPerPerformer = new HashMap<>();

	public void bufferClickInput(LivingEntity performer, LivingComponentAction performerAction, EntityActionType ability) {
		bufferPerPerformer.put(performer, new BufferedInputEntry(performerAction, ability, InputType.CLICK));
	}

	public HeldInput bufferHeldInput(LivingEntity performer, LivingComponentAction performerAction, EntityActionType ability) {
		BufferedInputEntry inputBuffer = new BufferedInputEntry(performerAction, ability, InputType.HOLD);
		bufferPerPerformer.put(performer, inputBuffer);
		return inputBuffer;
	}

	protected void tickInputBuffer() {
		if (user.level().isClientSide()) return;
		
		var entryIter = bufferPerPerformer.entrySet().iterator();
		while (entryIter.hasNext()) {
			var entry = entryIter.next();
			LivingEntity performer = entry.getKey();
			if (performer == null || !performer.isAlive()) {
				entryIter.remove();
			}
			else {
				BufferedInputEntry bufferedInput = entry.getValue();
				if (bufferedInput != null) {
					LivingComponentAction performerActionData = LivingComponentAction.getExistingComponent(performer);
					EntityActionType ability = bufferedInput.entityAbility;
					if (performerActionData != null && ability != null && !ability.shouldBufferInput(performerActionData)) {
						// Can finally start the previously buffered action
						EntityActionInstance newAction = ability.initActionOnAbilityUse(user.level(), user);
						performerActionData.setAction(newAction, user, SyncType.TRACKING_AND_SELF);
						for (var heldKeyAction : heldKeys.values()) {
							if (heldKeyAction.action == bufferedInput) {
								// Update the held key callback, to be able to stop the new action when the key is released by the player
								heldKeyAction.action = newAction;
								break;
							}
						}
						entryIter.remove();
					}
				}
			}
		}
	}


	public static record BufferedInputEntry(
			LivingComponentAction performerAction, 
			EntityActionType entityAbility, 
			InputType inputType) 
	implements HeldInput {

		@Override
		public void onStopHeld(LivingEntity user) {
			// Remove itself from the input buffer, if the key was released before the queued action could start

			EntityActionInputState inputState = user.getData(ModDataAttachmentTypes.ENTITY_ABILITY_INPUT.get());
			if (inputState != null) {
				var entryIter = inputState.bufferPerPerformer.entrySet().iterator();
				while (entryIter.hasNext()) {
					var entry = entryIter.next();
					if (entry.getValue() == this) {
						entryIter.remove();
						break;
					}
				}
			}
		}

	}
}
