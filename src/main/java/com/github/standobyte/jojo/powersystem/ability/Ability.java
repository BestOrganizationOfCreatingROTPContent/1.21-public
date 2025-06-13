package com.github.standobyte.jojo.powersystem.ability;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.powersystem.entityaction.HeldInput;

import net.minecraft.network.FriendlyByteBuf;
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
	
	
	public void writeExtraInput(FriendlyByteBuf serverboundBuf) {}

	// Input stuff below is called in AbilityInput
	
	@ApiStatus.OverrideOnly
	public void onClick(Level level, LivingEntity user, 
			FriendlyByteBuf extraClientInput, float clickHoldResolveTime) {}
	
	@ApiStatus.OverrideOnly
	@Nullable
	public HeldInput onButtonStartHold(Level level, LivingEntity user, 
			FriendlyByteBuf extraClientInput, float clickHoldResolveTime) {
		return null;
	}
	
}
