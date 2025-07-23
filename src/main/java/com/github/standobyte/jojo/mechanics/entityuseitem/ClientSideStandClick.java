package com.github.standobyte.jojo.mechanics.entityuseitem;

import java.util.List;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.input.AbilityInputState;
import com.github.standobyte.jojo.client.input.InputHandler;
import com.github.standobyte.jojo.client.input.controlscheme.AllControlSchemes;
import com.github.standobyte.jojo.client.input.controlscheme.ClientControlScheme;
import com.github.standobyte.jojo.client.input.controlscheme.ClientKeyWrapper;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent.InteractionKeyMappingTriggered;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class ClientSideStandClick {

	@SubscribeEvent
	public static void onMcClickInput(InteractionKeyMappingTriggered event) {
		if (event.isUseItem() && standCanRightClickItems) {
			StandEntity stand = ClientGlobals.playerStandEntity;
			if (stand != null) {
				if (stand.isManuallyControlled() || !InputHandler.holdingLAlt && ServerSideLivingClick.isEntityHoldingAnItem(stand)) {
					event.setCanceled(true);
					event.setSwingHand(false);
					
					HitResult target = Minecraft.getInstance().hitResult;
					if (event.isUseItem()) {
						PacketDistributor.sendToServer(new ClStandClickPacket(target, InteractionHand.MAIN_HAND, InteractionHand.OFF_HAND));
					}
					else {
						PacketDistributor.sendToServer(new ClStandClickPacket(target, event.getHand()));
					}
				}
			}
		}
	}
	
	public static void onMovesUpdate(Power<?> power, AvailableAbilities abilities) {
		StandPower standPower = PowerClass.STAND.cast(power);
		if (standPower != null) {
			hideMouseButtonStandKeybinds(standPower, abilities);
		}
	}
	
	private static boolean standCanRightClickItems = true;
	
	private static void hideMouseButtonStandKeybinds(StandPower power, AvailableAbilities abilities) {
		if (!standCanRightClickItems) return;
		
		if (ServerSideLivingClick.isEntityHoldingAnItem(power.getSummonedStandEntity())) {
			ClientControlScheme controlScheme = AllControlSchemes.controls.get(power.getPowerType().getId());
			if (controlScheme != null) {
				ClientKeyWrapper RMB = ClientKeyWrapper.make(InputConstants.Type.MOUSE, InputConstants.MOUSE_BUTTON_RIGHT);
				for (InputMethod inputMethod : InputMethod.values()) {
					List<String> rmbAbilities = controlScheme.getBindsWithModifier(inputMethod, RMB, KeyModifier.NONE);
					for (String abilityName : rmbAbilities) {
						var ability = abilities._inMoveset.get(abilityName);
						if (ability != null) {
							AbilityInputState inputState = AbilityInputState.withValue(ability.clientInputState);
							if (!inputState.getFlag(AbilityInputState.WITH_ITEM_HELD)) {
								inputState.setFlag(AbilityInputState.IS_ACTIVE, false);
								inputState.setFlag(AbilityInputState.VISIBLE_EVEN_INACTIVE, false);
								inputState.setFlag(AbilityInputState.VISIBLE_TRANSLUCENT, false);
							}
							ability.clientInputState = inputState._value;
						}
					}
				}
			}
		}
	}
	
}
