package com.github.standobyte.jojo.powersystem;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.util.entitycomponent.helpers.SynchronizablePlayerData;
import com.github.standobyte.jojo.util.entitycomponent.helpers.TickingEntityData;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;

public abstract class Power<P extends Power<P>> implements SynchronizablePlayerData, TickingEntityData, INBTSerializable<CompoundTag> {
	@Nonnull protected final LivingEntity user;
	protected final Optional<ServerPlayer> serverPlayerUser;
	
	public Power(LivingEntity user) {
		this.user = user;
		this.serverPlayerUser = Optional.ofNullable(user instanceof ServerPlayer player ? player : null);
		addSynchronization(user);
		addTicking(user);
	}
	
	
	@Override
	public void tick() {
		
	}
	
	
	@Nullable
	public abstract PowerType<P> getPowerType();
	
	@Nonnull
	public Moveset<P> getMoveset() {
		PowerType<P> powerType = getPowerType();
		return powerType != null ? powerType.getMoveset() : Moveset.empty();
	}
	
	@Nullable
	public Ability<P> getAbility(String name) {
		if (name == null) return null;
		if (!hasPower()) {
			JojoMod.getLogger().warn("Invalid state: {} tried to use ability {} with no {} power.", 
					user.getDisplayName().getString(), name, getClass());
			return null;
		}
		Ability<P> ability = getMoveset().getAbility(name);
		if (ability == null) {
			JojoMod.getLogger().warn("Invalid ability id: {} tried to use ability {} with {} power {}.", 
					user.getDisplayName().getString(), name, getClass(), getPowerType().getId());
		}
		return ability;
	}
	
	public boolean hasPower() {
		return getPowerType() != null;
	}
	
	public abstract PowerClass<P> getPowerClass();
	
	public LivingEntity getUser() {
		return user;
	}


	@Override
	public void syncToPlayer(ServerPlayer user) {
	}

	@Override
	public void syncToTracking(ServerPlayer player) {
	}
	
	@Override
	public void onPlayerClone(Player newPlayer, boolean wasDeath) {
		P newPower = getPowerClass().get(newPlayer);
		onPlayerCloneData(newPower, wasDeath);
	}
	
	protected void onPlayerCloneData(P newPower, boolean wasDeath) {}
	
	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag nbt = new CompoundTag();
		return nbt;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
	}
	
	
	@SuppressWarnings("unchecked")
	protected final P getThis() {
		return (P) this;
	}
}
