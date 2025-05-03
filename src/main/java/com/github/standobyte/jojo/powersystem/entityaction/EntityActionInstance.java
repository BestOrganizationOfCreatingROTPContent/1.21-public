package com.github.standobyte.jojo.powersystem.entityaction;

import java.util.Map;

import javax.annotation.Nonnull;

import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId.AbilityInputNetwork;

import net.minecraft.Util;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.LivingEntity;

// TODO (entity action) test the phase lengths stuff
public class EntityActionInstance {
	@Nonnull public final EntityAbility<?> ability;
	// TODO (entity action 2) allow for phase length editing before synchronizing the action
	protected Map<ActionPhase, Float> phasesLength;
	
	@Nonnull protected ActionPhase phase;
	protected int curPhaseTick;
	protected float curPhaseLength;
	protected float phasePartialTick;
	
	public EntityActionInstance(EntityAbility<?> ability) {
		this.ability = ability;
		this.phasesLength = Util.makeEnumMap(ActionPhase.class, phase -> {
			float length = ability.getPhaseLength(phase);
			return (phase == ActionPhase.PERFORM ? Math.max(length, 1) : Math.max(length, 0));
		});
		setPhase(ActionPhase.values()[0]);
	}

	// TODO (!) (entity action) partial tick for consecutive actions
	public void setPartialTick(float partialTick) {
		if (partialTick >= 1) throw new IllegalArgumentException();
		this.phasePartialTick = partialTick;
	}
	
	
	public void setPhase(ActionPhase phase) {
		setPhase(phase, 0);
	}
	
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
		
		this.phasePartialTick = prevPhaseTick - prevTickLength;
		
		checkNextPhase();
	}
	
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

	@SuppressWarnings("unchecked")
	protected <A extends EntityActionInstance> void tickSkippedNonZeroPhase() {
		if (curPhaseTick == 0 && curPhaseLength > 0) {
			A action = (A) this;
			EntityAbility<A> ability = (EntityAbility<A>) action.ability;
			ability.onEntityActionTick(action, performer, powerUser);
		}
	}
	
	
	protected LivingEntity performer;
	protected LivingEntity powerUser;
	@SuppressWarnings("unchecked")
	public <A extends EntityActionInstance> void onActionSet(LivingEntity performer, LivingEntity user) {
		this.performer = performer;
		A action = (A) this;
		EntityAbility<A> ability = (EntityAbility<A>) action.ability;
		this.powerUser = user;
		ability.onActionSet(action, performer, user);
	}
	
	/**
	 * @param performer The entity that is performing the action (in case of Stand abilities like punches, that would be the summoned StandEntity, not the user).
	 * @param power The power object of the matching PowerClass attached to the user.
	 * @return true if the action is over and should be set to null.
	 */
	@SuppressWarnings("unchecked")
	public <A extends EntityActionInstance> boolean tickAction() {
		if (isOver()) return true;
		A action = (A) this;
		EntityAbility<A> ability = (EntityAbility<A>) action.ability;
		ability.onEntityActionTick(action, performer, powerUser);
		this.postTickTimer();
		return isOver();
	}

	@SuppressWarnings("unchecked")
	public <A extends EntityActionInstance> void onActionCleared() {
		A action = (A) this;
		EntityAbility<A> ability = (EntityAbility<A>) action.ability;
		ability.onActionCleared(action, performer, powerUser);
	}
	
	
	public void forceStop() {
		setPhase(null);
	}
	
	private void postTickTimer() {
		++curPhaseTick;
		checkNextPhase();
	}
	
	public boolean isOver() {
		return phase == null;
	}
	
	
	public float getPhaseTick() {
		return curPhaseTick + phasePartialTick;
	}
	
	public float getPhaseTicksLeft() {
		return curPhaseLength - getPhaseTick();
	}
	
	public ActionPhase getPhase() {
		return phase;
	}
	
	public float getPhaseRatio(float renderPartialTick) {
		if (curPhaseLength == 0) throw new IllegalStateException();
		return (getPhaseTick() + renderPartialTick) / curPhaseLength;
	}
	
	
	public void toBuf(RegistryFriendlyByteBuf buf) {}
	
	public void fromBuf(RegistryFriendlyByteBuf buf) {}
	
	// TODO (entity action 2) nbt save/load (both stand and living)
	
	
	public static final StreamCodec<RegistryFriendlyByteBuf, EntityActionInstance> NETWORK_CODEC = new StreamCodec<>() {

		@Override
		public EntityActionInstance decode(RegistryFriendlyByteBuf buffer) {
			boolean valid = buffer.readBoolean();
			if (valid) {
				EntityAbility<?> ability = (EntityAbility<?>) AbilityInputNetwork.decodeInput(buffer).getAbility(null);
				if (ability != null) {
					EntityActionInstance action = ability.createEntityAction();
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
				AbilityInputNetwork.encodeInput(buffer, (Ability) action.ability, null);
				action.phasesLength.values().forEach(buffer::writeFloat);
				buffer.writeVarInt(action.phase.ordinal());
				buffer.writeVarInt(action.curPhaseTick);
				buffer.writeFloat(action.phasePartialTick);
				buffer.writeFloat(action.curPhaseLength);
				action.toBuf(buffer);
			}
		}
		
	};
	
}
