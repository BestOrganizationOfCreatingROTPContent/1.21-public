package com.github.standobyte.jojo.powersystem.entityaction;

import java.util.Map;

import javax.annotation.Nonnull;

import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityInputNetwork;

import net.minecraft.Util;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.LivingEntity;

// TODO (entity action) test the phase lengths stuff
public class EntityActionInstance {
	@Nonnull public final EntityAbility<?, ?> ability;
	protected Map<ActionPhase, Integer> phasesLength;
	
	@Nonnull protected ActionPhase phase;
	protected int curPhaseTick;
	protected int curPhaseLength;
	
	public EntityActionInstance(EntityAbility<?, ?> ability) {
		this.ability = ability;
		this.phasesLength = Util.makeEnumMap(ActionPhase.class, phase -> {
			float length = ability.getPhaseLength(phase);
			return (int) (phase == ActionPhase.PERFORM ? Math.max(length, 1) : Math.max(length, 0));
		});
		setPhase(ActionPhase.values()[0]);
	}
	
	protected EntityActionInstance(EntityAbility<?, ?> ability, Map<ActionPhase, Integer> phasesLength, @Nonnull ActionPhase phase, int tick) {
		this.ability = ability;
		this.phasesLength = phasesLength;
		this.phase = phase;
		this.curPhaseLength = phasesLength.get(phase);
	}
	
	
	public void setPhase(ActionPhase phase) {
		setPhase(phase, 0);
	}
	
	public void setPhase(ActionPhase phase, int tick) {
		this.phase = phase;
		this.curPhaseTick = tick;
		this.curPhaseLength = phase != null ? phasesLength.get(phase) : -1;
		checkNextPhase();
	}
	
	protected void checkNextPhase() {
		if (isOver()) return;
		ActionPhase[] phases = ActionPhase.values();
		if (phase != null && curPhaseTick >= curPhaseLength) {
			int ordinal = phase.ordinal() + 1;
			ActionPhase nextPhase = ordinal < phases.length ? phases[ordinal] : null;
			setPhase(nextPhase);
		}
	}
	
	
	private LivingEntity performer;
	private Power<?> power;
	@SuppressWarnings("unchecked")
	public <A extends EntityActionInstance, P extends Power<P>> void onActionSet(LivingEntity performer, LivingEntity user) {
		this.performer = performer;
		A action = (A) this;
		EntityAbility<A, P> ability = (EntityAbility<A, P>) action.ability;
		P power = user != null ? ((PowerClass<P>) ability.getPowerClass()).get(user) : null;
		this.power = power;
		ability.onActionSet(action, performer, power);
	}
	
	/**
	 * @param performer The entity that is performing the action (in case of Stand abilities like punches, that would be the summoned StandEntity, not the user).
	 * @param power The power object of the matching PowerClass attached to the user.
	 * @return true if the action is over and should be set to null.
	 */
	@SuppressWarnings("unchecked")
	public <A extends EntityActionInstance, P extends Power<P>> boolean tickAction() {
		if (isOver()) return true;
		A action = (A) this;
		EntityAbility<A, P> ability = (EntityAbility<A, P>) action.ability;
		ability.onEntityActionTick(action, performer, ability.getPowerClass().cast(power));
		this.postTickTimer();
		return isOver();
	}

	@SuppressWarnings("unchecked")
	public <A extends EntityActionInstance, P extends Power<P>> void onActionCleared() {
		A action = (A) this;
		EntityAbility<A, P> ability = (EntityAbility<A, P>) action.ability;
		ability.onActionCleared(action, performer, ability.getPowerClass().cast(power));
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
	
	
	public int getTick() {
		return curPhaseTick;
	}
	
	public int getTicksLeft() {
		return curPhaseLength - curPhaseTick;
	}
	
	public ActionPhase getPhase() {
		return phase;
	}
	
	public float getPhaseRatio(float partialTick) {
		if (curPhaseLength == 0) throw new IllegalStateException();
		return (curPhaseTick + partialTick) / curPhaseLength;
	}
	
	
	public void toBuf(RegistryFriendlyByteBuf buf) {}
	
	public void fromBuf(RegistryFriendlyByteBuf buf) {}
	
	// TODO (entity action 2) nbt save/load (both stand and living)
	
	
	public static final StreamCodec<RegistryFriendlyByteBuf, EntityActionInstance> NETWORK_CODEC = new StreamCodec<>() {

		@Override
		public EntityActionInstance decode(RegistryFriendlyByteBuf buffer) {
			boolean valid = buffer.readBoolean();
			if (valid) {
				EntityAbility<?, ?> ability = (EntityAbility<?, ?>) AbilityInputNetwork.decodeInput(buffer);
				if (ability != null) {
					EntityActionInstance action = ability.createEntityAction();
					action.phasesLength = Util.makeEnumMap(ActionPhase.class, __ -> buffer.readVarInt());
					action.phase = ActionPhase.values()[buffer.readVarInt()];
					action.curPhaseTick = buffer.readVarInt();
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
				AbilityInputNetwork.encodeInput(buffer, (Ability<?>) action.ability, null);
				action.phasesLength.values().forEach(buffer::writeVarInt);
				buffer.writeVarInt(action.phase.ordinal());
				buffer.writeVarInt(action.curPhaseTick);
				action.toBuf(buffer);
			}
		}
		
	};
	
}
