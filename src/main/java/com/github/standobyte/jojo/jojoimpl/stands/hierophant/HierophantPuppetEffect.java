package com.github.standobyte.jojo.jojoimpl.stands.hierophant;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.event.RipplesAbilityKeyPressEvent;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.init.power.ModStandAbilities;
import com.github.standobyte.jojo.mechanics.entitycontrol.ServerEntityController;
import com.github.standobyte.jojo.mechanics.possessionv2.LivingComponentPossession;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.effect.StandEffectInstance;
import com.github.standobyte.jojo.powersystem.standpower.effect.StandEffectType;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.util.StandUtil;
import com.github.standobyte.jojo.util.entitycomponent.ComponentUtil;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class HierophantPuppetEffect extends StandEffectInstance {

	public HierophantPuppetEffect(StandEffectType<?> effectType) {
		super(effectType);
		needsTarget = true;
	}

	@Override
	protected void start() {}

	@Override
	protected void tick() {}

	@Override
	protected void stop() {
		LivingEntity user = getStandUser();
		if (!user.level().isClientSide()) {
			setMobControl(false);
			
			StandEntity hierophant = getUserPower().getSummonedStandEntity();
			if (hierophant != null) {
				LivingComponentPossession.stopPossession(hierophant);
			}
		}
	}
	
	public void setMobControl(boolean control) {
		LivingEntity user = getStandUser();
		
		if (control) {
			if (getTargetLiving() instanceof Mob targetMob) {
				ServerEntityController.setServerControlTarget(user, targetMob, "mob");
			}
		}
		else {
			ServerEntityController component = ComponentUtil.getExistingDataOrNull(user, ModDataAttachmentTypes.CONTROLLER);
			if (component != null) {
				component.stopControlling(true);
			}
		}
		
		StandEntity hierophant = getUserPower().getSummonedStandEntity();
		if (hierophant != null) {
			hierophant.setManuallyControlled(control);
		}
	}

	
	
	@SubscribeEvent
	public static void onManualControlToggle(RipplesAbilityKeyPressEvent event) {
		Ability ability = event.getAbility();
		AbilityId abilityId = ability.abilityId;
		if (abilityId.powerClass() == PowerClass.STAND && abilityId.nameInMoveset().equals("manual_control")) {
			LivingEntity user = event.getEntity();
			StandPower standPower = StandPower.get(user);
			if (standPower != null) {
				HierophantPuppetEffect puppeting = standPower.userStandEffects
						.getEffectOfType(ModStandAbilities.EFFECT_HG_PUPPET.get())
						.orElse(null);
				if (puppeting != null) {
					event.setCanceled(true);
					
					if (!user.level().isClientSide()) {
						StandEntity stand = StandUtil.getSummonedStand(user);
						if (stand != null) {
							puppeting.setMobControl(!stand.isManuallyControlled());
						}
					}
				}
			}
		}
	}
}
