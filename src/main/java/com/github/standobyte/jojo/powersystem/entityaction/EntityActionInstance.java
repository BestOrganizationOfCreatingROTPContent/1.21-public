package com.github.standobyte.jojo.powersystem.entityaction;

import java.util.Map;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.powersystem.ability.Ability;

import net.minecraft.Util;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

// TODO (entity action) test the phase lengths stuff
public class EntityActionInstance implements HeldInput {
	@Nonnull public final EntityActionAbility ability;
	public Map<ActionPhase, Float> phasesLength;
	
	@Nonnull protected ActionPhase phase;
	protected int curPhaseTick;
	protected float curPhaseLength;
	protected float phasePartialTick;
	
	protected LivingEntity performer;
	protected LivingEntity powerUser;
	
	public EntityActionInstance(EntityActionAbility ability) {
		this.ability = ability;
		this.phasesLength = Util.makeEnumMap(ActionPhase.class, phase -> phase == ActionPhase.PERFORM ? 1f : 0f);
	}
	
	public void setPhaseZero() {
		setPhase(ActionPhase.values()[0]);
	}
	

	@ApiStatus.OverrideOnly
	public void onActionSet() {
		
	}

	@ApiStatus.OverrideOnly
	public void actionTick() {
		
	}

	@ApiStatus.OverrideOnly
	public void actionPerform() {
		
	}

	@ApiStatus.OverrideOnly
	public void onActionCleared() {
		
	}
	
	@ApiStatus.OverrideOnly
	public void onButtonStopHold() {
		
		
	}
	
	@ApiStatus.OverrideOnly
	public boolean canBeCancelledInto(Ability cancellingAbility) {
		return phase == ActionPhase.RECOVERY;
	}
	
	

	@ApiStatus.NonExtendable
	public float getPhaseTick() {
		return curPhaseTick + phasePartialTick;
	}

	@ApiStatus.NonExtendable
	public float getPhaseTicksLeft() {
		return curPhaseLength - getPhaseTick();
	}

	@ApiStatus.NonExtendable
	public ActionPhase getPhase() {
		return phase;
	}

	@ApiStatus.NonExtendable
	public float getPhaseRatio(float renderPartialTick) {
		if (curPhaseLength == 0) throw new IllegalStateException();
		return (getPhaseTick() + renderPartialTick) / curPhaseLength;
	}
	
	public float getFullTicksPassed() {
		float sum = 0;
		for (ActionPhase phase : ActionPhase.values()) {
			if (phase != this.phase) {
				sum += phasesLength.get(phase);
			}
			else {
				sum += getPhaseTick();
				break;
			}
		}
		return sum;
	}
	

	@ApiStatus.NonExtendable
	public void forceStop() {
		setPhase(null);
	}

	@ApiStatus.NonExtendable
	public boolean isOver() {
		return phase == null;
	}
	
	
	@ApiStatus.OverrideOnly
	public void toBuf(RegistryFriendlyByteBuf buf) {}

	@ApiStatus.OverrideOnly
	public void fromBuf(RegistryFriendlyByteBuf buf) {}
	
	
	

	// TODO (!) (entity action) partial tick for consecutive actions
	@ApiStatus.Internal
	public void setPartialTick(float partialTick) {
		if (partialTick >= 1) throw new IllegalArgumentException();
		this.phasePartialTick = partialTick;
	}
	
	@ApiStatus.Internal
	public void setPhase(ActionPhase phase) {
		setPhase(phase, 0);
	}

	@ApiStatus.Internal
	public void setPhase(ActionPhase phase, int tick) {
		if (phase == null) {
			this.phase = null;
			return;
		}
		
		float prevPhaseTick = getPhaseTick();
		float prevTickLength = this.curPhaseLength;
		
		this.phase = phase;
		this.curPhaseTick = tick;
		this.curPhaseLength = phase != null ? phasesLength.get(phase) : -1;
		
		this.phasePartialTick = Mth.clamp(prevPhaseTick - prevTickLength, 0, 1);
		
		checkNextPhase();
	}

	@ApiStatus.Internal
	protected void checkNextPhase() {
		if (isOver()) return;
		ActionPhase[] phases = ActionPhase.values();
		if (phase != null && getPhaseTick() >= curPhaseLength) {
			int ordinal = phase.ordinal() + 1;
			ActionPhase nextPhase = ordinal < phases.length ? phases[ordinal] : null;
			tickSkippedNonZeroPhase();
			setPhase(nextPhase);
		}
	}

	@ApiStatus.Internal
	protected void tickSkippedNonZeroPhase() {
		if (curPhaseTick == 0 && curPhaseLength > 0) {
			_onTick();
		}
	}
	
	
	@ApiStatus.Internal
	public void _onActionSet(LivingEntity performer, LivingEntity user) {
		this.performer = performer;
		this.powerUser = user;
		onActionSet();
	}
	
	/**
	 * @param performer The entity that is performing the action (in case of Stand abilities like punches, that would be the summoned StandEntity, not the user).
	 * @param power The power object of the matching PowerClass attached to the user.
	 * @return true if the action is over and should be set to null.
	 */
	@ApiStatus.Internal
	public boolean _tickAction() {
		if (isOver()) return true;
		_onTick();
		++curPhaseTick;
		checkNextPhase();
		return isOver();
	}
	
	@ApiStatus.Internal
	protected void _onTick() {
		actionTick();
		if (phase == ActionPhase.PERFORM && getPhaseTick() < 1) {
			actionPerform();
		}
	}

	@ApiStatus.Internal
	public void _onActionCleared() {
		onActionCleared();
	}

	@Override
	@ApiStatus.Internal
	public void onStopHeld(LivingEntity user) {
		if (!this.isOver()) {
			onButtonStopHold();
		}
	}
	
	
	public static final StreamCodec<RegistryFriendlyByteBuf, EntityActionInstance> NETWORK_CODEC = new StreamCodec<>() {

		@Override
		public EntityActionInstance decode(RegistryFriendlyByteBuf buffer) {
			boolean valid = buffer.readBoolean();
			if (valid) {
				EntityActionInstance action = EntityActionAbility.decodeAbilityAction(buffer);
				if (action != null) {
					action.phasesLength = Util.makeEnumMap(ActionPhase.class, __ -> buffer.readFloat());
					action.phase = ActionPhase.values()[buffer.readVarInt()];
					action.curPhaseTick = buffer.readVarInt();
					action.phasePartialTick = buffer.readFloat();
					action.curPhaseLength = buffer.readFloat();
					action.fromBuf(buffer);
					return action;
				}
			}
			
			return null;
		}

		@Override
		public void encode(RegistryFriendlyByteBuf buffer, EntityActionInstance action) {
			buffer.writeBoolean(action.phase != null);
			if (action.phase != null) {
				action.ability.encodeAbility(buffer);
				action.phasesLength.values().forEach(buffer::writeFloat);
				buffer.writeVarInt(action.phase.ordinal());
				buffer.writeVarInt(action.curPhaseTick);
				buffer.writeFloat(action.phasePartialTick);
				buffer.writeFloat(action.curPhaseLength);
				action.toBuf(buffer);
			}
		}
		
	};
	
	// TODO (entity action 2) nbt save/load (both stand and living)
	
}
