package com.github.standobyte.jojo.powersystem.standpower.entity;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.standobyte.jojo.core.molang.MolangValue;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.entityaction.ActionAnimIdentifier;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityAbility;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.util.network.NetworkUtil;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class StandEntityAbility<A extends EntityActionInstance> extends Ability implements EntityAbility<A> {
	protected ActionAnimIdentifier standAnim;

	public StandEntityAbility(AbilityId abilityId) {
		super(abilityId);
		this.standAnim = ActionAnimIdentifier.getOrCreate(abilityId);
	}
	
	
	protected MolangValue windupLength = new MolangValue.Literal(0);
	protected MolangValue performLength = new MolangValue.Literal(1);
	protected MolangValue recoveryLength = new MolangValue.Literal(0);
	
	@Override
	public float getPhaseLength(ActionPhase phase) {
		return switch (phase) {
			case WINDUP -> windupLength.getAsFloat();
			case PERFORM -> performLength.getAsFloat();
			case RECOVERY -> recoveryLength.getAsFloat();
		};
	}
	
	protected void initPhaseLength(ActionPhase phase, float length) {
		switch (phase) {
			case WINDUP -> windupLength = new MolangValue.Literal(length);
			case PERFORM -> performLength = new MolangValue.Literal(length);
			case RECOVERY -> recoveryLength = new MolangValue.Literal(length);
		}
	}
	
	
	@Override
	public void onClick(Level level, LivingEntity user) {
		setStandAction(level, user);
	}
	
	@Override
	public A onButtonStartHold(Level level, LivingEntity user) {
		return setStandAction(level, user);
	}
	
	public A setStandAction(Level level, LivingEntity user) {
		StandPower power = PowerClass.STAND.get(user); if (power == null) return null;
		StandEntity standEntity = power.getSummonedStandEntity(); if (standEntity == null) return null;
		
		A action = createEntityAction();
		// onClick() and onButtonStartHold() calls are already sent to other clients, 
		// the only case where we need to actually sync the StandEntity's EntityActionInstance as it is
		// is when the user's entity is too far away for the client to call either of the methods above
		if (user != standEntity && !standEntity.isFollowingUser()) {
			Set<ServerPlayerConnection> trackingUser = NetworkUtil.getTrackingPlayers(user).collect(Collectors.toSet());
			Stream<ServerPlayer> trackingOnlyStand = NetworkUtil.getTrackingPlayers(standEntity)
					.filter(player -> !trackingUser.contains(player))
					.map(ServerPlayerConnection::getPlayer);
			standEntity.setStandAction(action, trackingOnlyStand);
		}
		else {
			standEntity.setStandAction(action, false);
		}
		return action;
	}

	@Override
	public ActionAnimIdentifier getEntityAnim() {
		return standAnim;
	}
	
}
