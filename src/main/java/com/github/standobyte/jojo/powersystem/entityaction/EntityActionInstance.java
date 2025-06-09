package com.github.standobyte.jojo.powersystem.entityaction;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.powersystem.entityaction.netcode.TrEntityActionPhaseTimePacket;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandOffsetFromUser;
import com.github.standobyte.jojo.util.mc.EntityResolver;
import com.github.standobyte.jojo.util.network.NetworkUtil;
import com.github.standobyte.jojo.util.target.ActionTarget;

import net.minecraft.Util;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

// TODO (entity action) test the phase lengths stuff (with partial lengths and lengths < 1)
public class EntityActionInstance implements HeldInput {
	/** Is used in network code, to make sure server and client are on the same page when sending changes to the action's phases from server */
	@ApiStatus.Internal public int id;
	@Nonnull public final EntityActionType ability;
	@ApiStatus.Internal public Map<ActionPhase, Float> phasesLength;
	
	@Nonnull protected ActionPhase phase;
	protected int curPhaseTick;
	protected float curPhaseLength;
	protected float phasePartialTick;
	protected boolean stoppedHolding = false;
	
	protected LivingEntity performer;
	protected EntityResolver powerUser = new EntityResolver();
	
	@Nullable public ActionTarget standAimTarget;
	@Nullable public ActionTarget rotateStandTowardsTarget;
	
	public float userWalkSpeed = 1;
	
	public EntityActionInstance(EntityActionType ability) {
		this.ability = ability;
		this.phasesLength = new EnumMap<>(ActionPhase.class);
	}
	
	public void setPhaseZero() {
		startPhase(ActionPhase.values()[0]);
	}
	

	/**
	 * Is called before the action is synched from the server.
	 */
	@ApiStatus.OverrideOnly
	public void onActionSet(@Nullable EntityActionInstance prevAction) {
		
	}

	@ApiStatus.OverrideOnly
	public void actionTick() {
		
	}

	@ApiStatus.OverrideOnly
	public void actionPerformStart() {
		
	}

	@ApiStatus.OverrideOnly
	public void actionPerformEnd() {
		
	}

	@ApiStatus.OverrideOnly
	public void onSetPhase(ActionPhase newPhase) {
		
	}

	@ApiStatus.OverrideOnly
	public void onActionCleared(@Nullable EntityActionInstance newAction) {
		
	}
	
	@ApiStatus.OverrideOnly
	public void onButtonStopHold() {
		
	}
	
	@ApiStatus.OverrideOnly
	public boolean canBeCancelledInto(EntityActionType cancellingAbility) {
		return phase == ActionPhase.RECOVERY;
	}
	
	
	@ApiStatus.OverrideOnly
	public void toBuf(RegistryFriendlyByteBuf buf) {}

	@ApiStatus.OverrideOnly
	public void fromBuf(RegistryFriendlyByteBuf buf) {}
	
	
	// Some helper methods to write less boilerplate in Stand abilities
	
	protected void setStandOffset(double left, double front, StandOffsetFromUser.OffsetMode offsetMode, boolean changeOnlyIfIdle) {
		if (performer instanceof StandEntity standEntity) {
			LivingEntity user = getPowerUser();
			if (user != null && (!changeOnlyIfIdle || standEntity.offsetFromUser.isIdle())) {
				standEntity.offsetFromUser.setOffset(
						new Vec3(left, StandEntity.Y_OFFSET, front), 
						offsetMode, 
						user);
				standEntity.offsetFromUser.standAbility = this.ability;
			}
		}
	}
	
	public void keepStandAimedAtTarget() {
		if (!level().isClientSide()) {
			ActionTarget aimTarget = LivingComponentAction.getAim(performer).getTarget();
			if (!aimTarget.isEmpty(level())) {
				standAimTarget = aimTarget;
			}
		}
		rotateStandTowardsTarget = standAimTarget;
	}
	
	public final float calcFullTicks(ActionPhase targetPhase, float targetPhaseTick) {
		float sum = 0;
		for (ActionPhase phase : ActionPhase.values()) {
			float length = phasesLength.get(phase);
			if (phase == targetPhase) {
				length = Math.min(length, targetPhaseTick);
			}
			sum += length;
			if (phase == targetPhase) break;
		}
		return sum;
	}
	
	/**
	 * A function to time the punch swing sounds a few ticks before the actual punch impact
	 */
	public final boolean soundTiming(ActionPhase targetPhase, float targetPhaseTick, int soundOffset) {
		float ticksPassed = getFullTicksPassed();
		int ticksDiff = (int) (ticksPassed - calcFullTicks(targetPhase, targetPhaseTick));
		return ticksDiff == soundOffset
				|| soundOffset < 0 && soundOffset < ticksDiff && (int) ticksPassed == 0
				/*|| soundOffset > 0 && ... */;
	}
	
	public final boolean isUserCreative() {
		LivingEntity user = getPowerUser();
		return user instanceof Player player && player.getAbilities().instabuild;
	}
	
	protected Level level() {
		return performer.level();
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
		return calcFullTicks(this.phase, this.getPhaseTick());
	}
	

	@ApiStatus.NonExtendable
	public void forceStop() {
		startPhase(null);
	}

	@ApiStatus.NonExtendable
	public boolean isOver() {
		return phase == null;
	}
	
	
	public LivingEntity getPerformer() {
		return performer;
	}
	
	public LivingEntity getPowerUser() {
		if (performer != null) {
			return powerUser.getEntityLiving(performer.level());
		}
		return null;
	}
	
	
	

	// TODO (!) (entity action) partial tick for consecutive actions
	@ApiStatus.Internal
	public void setPartialTick(float partialTick) {
		if (partialTick >= 1) throw new IllegalArgumentException();
		this.phasePartialTick = partialTick;
	}
	
	public void startPhase(ActionPhase phase) {
		setPhase(phase, 0);
	}

	public void setPhase(ActionPhase phase, int tick) {
		if (phase == null) {
			this.phase = null;
			return;
		}
		
		float prevPhaseTick = getPhaseTick();
		float prevTickLength = this.curPhaseLength;
		
		if (this.performer != null && this.phase != phase) {
			onSetPhase(phase);
		}
		this.phase = phase;
		this.curPhaseTick = tick;
		this.curPhaseLength = phase != null ? phasesLength.get(phase) : -1;
		
		this.phasePartialTick = Mth.clamp(prevPhaseTick - prevTickLength, 0, 1);
		
		checkNextPhase();
	}
	
	public void syncPhaseChanges() {
		if (performer != null && !performer.level().isClientSide()) {
			PacketDistributor.sendToPlayersTrackingEntityAndSelf(performer, new TrEntityActionPhaseTimePacket(performer.getId(), 
					id, phasesLength, phase, curPhaseTick));
		}
	}


	@ApiStatus.Internal
	public void _onActionStarted(@Nullable EntityActionInstance prevAction) {
		onActionSet(prevAction);
		onSetPhase(phase);
	}

	@ApiStatus.Internal
	public void _beforeActionRemoved(@Nullable EntityActionInstance newAction) {
		onActionCleared(newAction);
	}
	
	@ApiStatus.Internal
	public void _tickAction() {
		if (!isOver()) {
			_onTick();
			++curPhaseTick;
			checkNextPhase();
		}
	}
	
	@ApiStatus.Internal
	protected void _onTick() {
		actionTick();
		if (phase == ActionPhase.PERFORM) {
			if (getPhaseTick() < 1) {
				actionPerformStart();
			}
			if (getPhaseTick() + 1 >= curPhaseLength) {
				actionPerformEnd();
			}
		}
	}
	
	@ApiStatus.Internal
	protected void checkNextPhase() {
		if (!isOver() && getPhaseTick() >= curPhaseLength) {
			// tick a skipped non-zero phase
			if (curPhaseTick == 0 && curPhaseLength > 0 && curPhaseLength <= 1) {
				_onTick();
			}
			
			int ordinal = phase.ordinal() + 1;
			ActionPhase nextPhase = ordinal < ActionPhase.values().length ? ActionPhase.values()[ordinal] : null;
			startPhase(nextPhase);
		}
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
		public void encode(RegistryFriendlyByteBuf buffer, EntityActionInstance action) {
			buffer.writeBoolean(action.phase != null);
			if (action.phase != null) {
				action.ability.encodeAbility(buffer);

				buffer.writeVarInt(action.id);
				action.phasesLength.values().forEach(buffer::writeFloat);
				buffer.writeVarInt(action.phase.ordinal());
				buffer.writeVarInt(action.curPhaseTick);
				buffer.writeFloat(action.phasePartialTick);
				buffer.writeFloat(action.curPhaseLength);
				action.powerUser.writeNetwork(buffer);
				NetworkUtil.writeOptionally(action.standAimTarget, buffer, ActionTarget.STREAM_CODEC_UNRESOLVED_ENTITY_ID);
				action.toBuf(buffer);
			}
		}

		@Override
		public EntityActionInstance decode(RegistryFriendlyByteBuf buffer) {
			boolean valid = buffer.readBoolean();
			if (valid) {
				EntityActionInstance action = EntityActionType.decodeAbilityAction(buffer);
				if (action != null) {
					action.id = buffer.readVarInt();
					action.phasesLength = Util.makeEnumMap(ActionPhase.class, __ -> buffer.readFloat());
					action.phase = ActionPhase.values()[buffer.readVarInt()];
					action.curPhaseTick = buffer.readVarInt();
					action.phasePartialTick = buffer.readFloat();
					action.curPhaseLength = buffer.readFloat();
					action.powerUser.readNetwork(buffer);
					action.standAimTarget = NetworkUtil.readOptional(buffer, ActionTarget.STREAM_CODEC_UNRESOLVED_ENTITY_ID).orElse(null);
					action.fromBuf(buffer);
					return action;
				}
			}
			
			return null;
		}
		
	};
	
}
