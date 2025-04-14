package com.github.standobyte.jojo.powersystem.ability;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

// XXX tick the unlocked abilities (passives are also abilities that aren't in the HUD)
public abstract class Ability<P extends Power<P>> {
	public final AbilityId abilityId;

	public Ability(AbilityId abilityId) {
		this.abilityId = abilityId;
	}
	
	
	public void writeExtraInput(RegistryFriendlyByteBuf serverboundBuf) {}
	
	@ApiStatus.OverrideOnly
	public void onClick(Level level, LivingEntity user, P power) {}
	
	@ApiStatus.OverrideOnly
	@Nullable
	public EntityActionInstance onButtonStartHold(Level level, LivingEntity user, P power) {
		return null;
	}

	@ApiStatus.OverrideOnly
	public void onButtonStopHold(Level level, LivingEntity user, P power, @Nullable EntityActionInstance heldAction) {
		if (heldAction != null) {
			// FIXME (!!!) onButtonStopHold (different abilities): 
			// if WINDUP, stop the action (with a short fade out anim)
			// if barrage/block/other held ability - setPhase(RECOVERY)
			heldAction.setPhase(ActionPhase.RECOVERY);
		}
	}
	
	public abstract PowerClass<P> getPowerClass();
	
}
