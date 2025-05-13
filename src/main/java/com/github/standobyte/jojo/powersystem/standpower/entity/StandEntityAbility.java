package com.github.standobyte.jojo.powersystem.standpower.entity;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.packet.fromserver.TrEntityActionInstancePacket;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.entityaction.ActionAnimIdentifier;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionAbility;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.HeldInput;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.util.network.NetworkUtil;
import com.github.standobyte.jojo.util.network.PacketDistributor2;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class StandEntityAbility extends Ability implements EntityActionAbility {
	protected ActionAnimIdentifier standAnim;

	public StandEntityAbility(AbilityId abilityId) {
		super(abilityId);
		this.standAnim = ActionAnimIdentifier.getOrCreate(abilityId);
	}
	
	
	@Override
	public void onClick(Level level, LivingEntity user) {
		StandPower power = PowerClass.STAND.get(user); if (power == null) return;
		StandEntity standEntity = power.getSummonedStandEntity(); if (standEntity == null) return;
		setStandAction(this, level, user, power, standEntity, true);
	}
	
	@Override
	public HeldInput onButtonStartHold(Level level, LivingEntity user) {
		StandPower power = PowerClass.STAND.get(user); if (power == null) return null;
		StandEntity standEntity = power.getSummonedStandEntity(); if (standEntity == null) return null;
		return setStandAction(this, level, user, power, standEntity, true);
	}
	
	public static EntityActionInstance setStandAction(StandEntityAbility ability, Level level, LivingEntity user, 
			StandPower power, StandEntity standEntity, boolean playerClickedAbility) {
		EntityActionInstance action = ability.createStandAction(level, user, power, standEntity);
		
		// onClick() and onButtonStartHold() calls are already sent to other clients.
		boolean sync = !playerClickedAbility;
		standEntity.getStandActionComponent().setAction(action, sync);
		/* 
		 * The only case where we need to actually sync the StandEntity's EntityActionInstance as it is
		 * is when the user's entity is too far away for the client to call either of the methods above
		 * (in case it's a long-ranged stand, the user might actually be outside of render distance for some players)
		 */ 
		if (!level.isClientSide() && user != standEntity && !standEntity.isFollowingUser()) {
			Set<ServerPlayerConnection> trackingUser = NetworkUtil.getTrackingPlayers(user).collect(Collectors.toSet());
			Stream<ServerPlayer> trackingOnlyStand = NetworkUtil.getTrackingPlayers(standEntity)
					.filter(player -> !trackingUser.contains(player))
					.map(ServerPlayerConnection::getPlayer);
			PacketDistributor2.sendToPlayers(standEntity, trackingOnlyStand, 
					false, new TrEntityActionInstancePacket(standEntity.getId(), action));
		}
		return action;
	}
	
	public EntityActionInstance createStandAction(Level level, LivingEntity user, StandPower power, StandEntity standEntity) {
		return initActionOnAbilityUse(level, user);
	}

	
	@Override
	@Nullable
	public LivingEntity getPerformer(LivingEntity user) {
		StandPower power = PowerClass.STAND.get(user);
		return power != null ? power.getSummonedStandEntity() : null;
	}

	@Override
	public ActionAnimIdentifier getEntityAnim(EntityActionInstance action) {
		return standAnim;
	}
	
}
