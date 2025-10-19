package com.github.standobyte.jojo.jojoimpl.stands.hierophant;

import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.init.power.ModStandAbilities;
import com.github.standobyte.jojo.mechanics.entitycontrol.ServerEntityController;
import com.github.standobyte.jojo.mechanics.entitycontrol.SetClientControllerPacket;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.HeldInput;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.effect.StandEffectInstance;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;
import com.github.standobyte.jojo.util.entitycomponent.ComponentUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

public class HierophantPuppetAbility extends StandEntityAbility {

	public HierophantPuppetAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId);
		initVariationAssets();
	}
	
	
	@Override
	public HeldInput onKeyPress(Level level, LivingEntity user, FriendlyByteBuf extraClientInput, 
			InputMethod inputMethod, float clickHoldResolveTime) {
		if (!level.isClientSide()) {
			StandPower power = PowerClass.STAND.get(user);
			if (power != null) {
				Optional<HierophantPuppetEffect> effect = power.userStandEffects.getEffectOfType(ModStandAbilities.EFFECT_HG_PUPPET.get());
				if (effect.isPresent()) {
					effect.get().remove();

					ServerEntityController component = ComponentUtil.getExistingDataOrNull(user, ModDataAttachmentTypes.CONTROLLER);
					if (component != null) {
						component.stopControlling();
						if (user instanceof ServerPlayer player) {
							PacketDistributor.sendToPlayer(player, new SetClientControllerPacket(-1, ""));
						}
					}
					
					return null;
				}
			}
		}
		
		return super.onKeyPress(level, user, extraClientInput, inputMethod, clickHoldResolveTime);
	}
	
	
	@Override
	public EntityActionInstance createActionObj() {
		return new PuppetingAction(this);
	}
	
	public static class PuppetingAction extends EntityActionInstance {
		public LivingEntity targetEntity;

		public PuppetingAction(EntityActionType ability) {
			super(ability);
		}
		
		@Override
		public void onActionSet(@Nullable EntityActionInstance prevAction) {
			keepStandAimedAtTarget();
			if (standRotationTarget != null && standRotationTarget.getEntity() instanceof LivingEntity targetLiving) {
				this.targetEntity = targetLiving;
			}
		}
		
		@Override
		public void actionPerformEnd() {
			Level level = level();
			if (!level.isClientSide()) {
				LivingEntity standUser = getPowerUser();
				StandPower power = StandPower.get(standUser);
				if (power != null) {
					power.userStandEffects.getEffectsOfType(ModStandAbilities.EFFECT_HG_PUPPET.get())
						.forEach(StandEffectInstance::remove);
					
					if (targetEntity != null) {
						HierophantPuppetEffect newEffect = ModStandAbilities.EFFECT_HG_PUPPET.get().create(level);
						power.userStandEffects.addEffect(newEffect.withTarget(targetEntity));

						if (targetEntity instanceof Mob) {
							ServerEntityController.setServerControlTarget(standUser, targetEntity, "mob");
						}
					}
				}
			}
		}

	}

	
	public static boolean hasPuppetUnderControl(StandPower userPower) {
		return userPower.userStandEffects.getEffectOfType(ModStandAbilities.EFFECT_HG_PUPPET.get()).isPresent();
	}

	protected String releaseSpriteName;
	protected Component releaseAbilityName;
	
	protected void initVariationAssets() {
		this.releaseSpriteName = this.spriteName + "_release";
		this.releaseAbilityName = abilityName(abilityId, ".release");
	}
	
	@Override
	public String getSpriteName(Power<?> context) {
		if (hasPuppetUnderControl(PowerClass.STAND.cast(context))) {
			return releaseSpriteName;
		}
		return super.getSpriteName(context);
	}

	// TODO ability names in stand skins
	@Override
	public Component getName(Power<?> context) {
		if (hasPuppetUnderControl(PowerClass.STAND.cast(context))) {
			return releaseAbilityName;
		}
		return name;
	}

}
