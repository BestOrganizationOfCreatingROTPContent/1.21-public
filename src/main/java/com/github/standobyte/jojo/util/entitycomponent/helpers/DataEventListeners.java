package com.github.standobyte.jojo.util.entitycomponent.helpers;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

// TODO test if this works
public class DataEventListeners {
	private List<SynchronizableEntityData> entityDataSync = new ArrayList<>();
	private List<SynchronizablePlayerData> playerDataSync = new ArrayList<>();
	private List<TickingEntityData> ticking = new ArrayList<>();
	
	public DataEventListeners(IAttachmentHolder entity) {}
	
	
	@ApiStatus.Internal
	public void addEntityDataSync(SynchronizableEntityData data) {
		this.entityDataSync.add(data);
	}
	
	@ApiStatus.Internal
	public void addPlayerDataSync(SynchronizablePlayerData data) {
		this.entityDataSync.add(data);
		this.playerDataSync.add(data);
	}
	
	public void addTickingData(TickingEntityData data) {
		this.ticking.add(data);
	}
	
	
	public void onTracking(ServerPlayer tracking) {
		for (var listener : entityDataSync) {
			listener.syncToTracking(tracking);
		}
	}
	
	public void onSyncToPlayer(ServerPlayer player) {
		for (var listener : playerDataSync) {
			listener.syncToPlayer(player);
		}
	}
	
	public void onClone(Player newPlayer, boolean wasDeath) {
		for (var listener : playerDataSync) {
			listener.onPlayerClone(newPlayer, wasDeath);
		}
	}
	
	public void onTick() {
		for (var listener : ticking) {
			listener.tick();
		}
	}
}
