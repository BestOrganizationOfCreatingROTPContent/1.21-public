package com.github.standobyte.jojo.powersystem.ability;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.core.molang.MolangValue;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.HeldInput;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

// XXX tick the unlocked abilities (passives are also abilities that aren't in the HUD)
public class Ability {
	public final AbilityId abilityId;

	public Ability(AbilityId abilityId) {
		this.abilityId = abilityId;
	}
	
	@ApiStatus.OverrideOnly
	@Nullable
	public Ability replaceWithSubAbility(LivingEntity user) {
		return null;
	}
	
	@ApiStatus.Internal
	public static Ability resolveSubAbility(Ability baseAbility, LivingEntity user) {
		if (baseAbility != null) {
			Ability subAbility = baseAbility.replaceWithSubAbility(user);
			return subAbility != null ? subAbility : baseAbility;
		}
		return null;
	}
	
	
	public void writeExtraInput(RegistryFriendlyByteBuf serverboundBuf) {}
	
	@ApiStatus.OverrideOnly
	public void onClick(Level level, LivingEntity user) {}
	
	@ApiStatus.OverrideOnly
	@Nullable
	public HeldInput onButtonStartHold(Level level, LivingEntity user) {
		return null;
	}
	
	// Some entity ability stuff, for the sake of configs, is also here
	
	@ApiStatus.OverrideOnly
	public void initActionFromConfig(EntityActionInstance action, Level level, LivingEntity user) {
		action.phasesLength.put(ActionPhase.WINDUP, windupLength.getAsFloat());
		action.phasesLength.put(ActionPhase.PERFORM, performLength.getAsFloat());
		action.phasesLength.put(ActionPhase.RECOVERY, recoveryLength.getAsFloat());
	}
	
	protected MolangValue windupLength = new MolangValue.Literal(0);
	protected MolangValue performLength = new MolangValue.Literal(1);
	protected MolangValue recoveryLength = new MolangValue.Literal(0);
	
	public void setDefaultPhaseLength(ActionPhase phase, float length) {
		switch (phase) {
			case WINDUP -> windupLength = new MolangValue.Literal(length);
			case PERFORM -> performLength = new MolangValue.Literal(length);
			case RECOVERY -> recoveryLength = new MolangValue.Literal(length);
		}
	}
	
}
