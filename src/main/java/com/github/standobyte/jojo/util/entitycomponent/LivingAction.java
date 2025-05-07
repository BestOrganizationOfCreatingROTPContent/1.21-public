package com.github.standobyte.jojo.util.entitycomponent;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.packet.fromserver.TrEntityActionInstancePacket;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.standpower.entity.LivingReactToNewAction;
import com.github.standobyte.jojo.util.entitycomponent.helpers.SynchronizablePlayerData;
import com.github.standobyte.jojo.util.entitycomponent.helpers.TickingEntityData;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.network.PacketDistributor;

public class LivingAction implements SynchronizablePlayerData, TickingEntityData, INBTSerializable<CompoundTag> {
	private final LivingEntity entity;
	private final LivingReactToNewAction actionSetCallback;
	@Nullable private EntityActionInstance action;
	
	public LivingAction(LivingEntity entity) {
		this.entity = entity;
		this.actionSetCallback = (entity instanceof LivingReactToNewAction standEntity) ? standEntity : null;
		addSynchronization(entity);
	}
	
	public EntityActionInstance getAction() {
		return action;
	}
	
	public void setAction(EntityActionInstance action, boolean sync) {
		if (actionSetCallback != null && actionSetCallback.onActionSet(action)) {
			return;
		}
		
		if (this.action != null) {
			this.action._onActionCleared();
		}
		this.action = action;
		if (action != null) {
			action._onActionSet(entity, entity);
		}
		
		if (sync && !entity.level().isClientSide()) {
			PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, new TrEntityActionInstancePacket(
					entity.getId(), action));
		}
	}
	
	
	@Override
	public void tick() {
		if (action != null) {
			tickAction();
		}
	}
	
	protected void tickAction() {
		if (action._tickAction()) {
			setAction(null, false);
		}
	}
	
	
	// TODO (entity action 2) sync on load
	@Override
	public void syncToPlayer(ServerPlayer player) {
	}

	// TODO (entity action 2) sync already existing action with tracking
	@Override
	public void syncToTracking(ServerPlayer player) {
	}
	
	@Override
	public void onPlayerClone(Player newPlayer, boolean wasDeath) {}


	@Override
	public CompoundTag serializeNBT(Provider provider) {
		CompoundTag nbt = new CompoundTag();
		
		return nbt;
	}

	@Override
	public void deserializeNBT(Provider provider, CompoundTag nbt) {
		
	}
	
	
	public static LivingAction create(IAttachmentHolder obj) {
		if (obj instanceof LivingEntity entity) {
			return new LivingAction(entity);
		}
		throw new IllegalArgumentException();
	}
	
	public static LivingAction getComponent(LivingEntity entity) {
		return entity.getData(ModDataAttachmentTypes.LIVING_ACTION.get());
	}
	
	@Nullable
	public static LivingAction getExistingComponent(LivingEntity entity) {
		AttachmentType<LivingAction> t = ModDataAttachmentTypes.LIVING_ACTION.get();
		return entity.hasData(t) ? entity.getData(t) : null;
	}
	
	@Nullable
	public static EntityActionInstance getUserAction(LivingEntity entity) {
		LivingAction existingData = getExistingComponent(entity);
		return existingData != null ? existingData.getAction() : null;
	}

}
