package com.github.standobyte.jojo;

import java.util.List;
import java.util.Map;

import com.github.standobyte.jojo.client.input.ClientPowerCache;
import com.github.standobyte.jojo.client.input.InputHandler;
import com.github.standobyte.jojo.client.input.controlscheme.AllControlSchemes;
import com.github.standobyte.jojo.client.input.controlscheme.ClientControlScheme;
import com.github.standobyte.jojo.client.input.controlscheme.ClientControlScheme.BindsByModifier;
import com.github.standobyte.jojo.client.input.controlscheme.ClientControlScheme.Hotbar;
import com.github.standobyte.jojo.client.input.controlscheme.ClientKeyWrapper;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.ability.Ability.AbilityInputActive;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities.AbilityConditionCheck;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.NeoForge;

@EventBusSubscriber(value = Dist.CLIENT, bus = Bus.MOD)
public class DebugStandHud {
	
	public static PrototypeAbilityHud abilityHUDInstance;

	@SubscribeEvent
	public static void addHud(RegisterGuiLayersEvent event) {
		event.registerAboveAll(JojoMod.resLoc("ability_hud"), abilityHUDInstance = new PrototypeAbilityHud());
		NeoForge.EVENT_BUS.register(new GameBusEventHandler());
	}
	
	public static class GameBusEventHandler {
		
		@SubscribeEvent
		public void onContainerMenuRender(ContainerScreenEvent.Render.Foreground event) {
			GuiGraphics graphics = event.getGuiGraphics();
			AbstractContainerScreen<?> screen = event.getContainerScreen();
			graphics.pose().pushPose();
			graphics.pose().translate(-screen.getGuiLeft(), -screen.getGuiTop(), 0.0F);
			abilityHUDInstance.renderAbilitiesHUD(graphics, Minecraft.getInstance().getDeltaTracker(), true);
			graphics.pose().popPose();
		}
		
	}
	
	public static class PrototypeAbilityHud implements LayeredDraw.Layer {
		
		@Override
		public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
			Minecraft mc = Minecraft.getInstance();
			if (!(mc.screen instanceof AbstractContainerScreen)) {
				renderAbilitiesHUD(guiGraphics, deltaTracker, false);
			}
			
		}

		public void renderAbilitiesHUD(GuiGraphics guiGraphics, DeltaTracker deltaTracker, boolean inContainerMenu) {
			Minecraft mc = Minecraft.getInstance();
			if (mc.options.hideGui) return;
			InputHandler input = InputHandler.getInstance();
			if (input == null) return;
			
			Player player = mc.player;
			Power<?> power = input.getCurPower();
			if (player == null || power == null || !power.hasPower()) return;
			
			int x = 10;
			int y = 10;
			Font font = mc.font;
			
			int color = 0xFFFFFFFF;
			if (power instanceof StandPower standPower) {
				StandSkin skin = StandSkinsLoader.getInstance().getSkin(standPower);
				if (skin != null) {
					color = skin.getColor();
				}
			}
			
			ClientControlScheme controlScheme = AllControlSchemes.controls.get(power.getPowerType().getId());
			if (controlScheme != null) {
				KeyModifier modifier = input.getCurModifier();
				
				AvailableAbilities available = ClientPowerCache.getAvailableMoves(power.getPowerClass(), power);
				Map<ClientKeyWrapper, Map<InputMethod, BindsByModifier<List<String>>>> binds = controlScheme.binds;
				for (var bindEntry : binds.entrySet()) {
					ClientKeyWrapper key = bindEntry.getKey();
					for (var byInputMethod : bindEntry.getValue().entrySet()) {
						List<String> boundAbilities = byInputMethod.getValue().withCurrentModifier(modifier);
						AbilityConditionCheck ability = ClientControlScheme.prioritizedAbility(boundAbilities, available, false);
						InputMethod inputMethod = byInputMethod.getKey();
						if (renderAbilityName(key, inputMethod, ability, x, y, color, guiGraphics, font, inContainerMenu)) {
							y += 9;
						}
					}
				}
				
				for (Hotbar hotbar : controlScheme.hotbars) {
					y += 5;
					if (!hotbar.slots.isEmpty()) {
						ClientKeyWrapper key = hotbar.useAbilityKey;
						List<Map<InputMethod, BindsByModifier<String>>> slots = hotbar.slots;
						Map<InputMethod, BindsByModifier<String>> slot = slots.get(hotbar.slotIndex < slots.size() ? hotbar.slotIndex : 0);
						for (var byInputMethod : slot.entrySet()) {
							InputMethod inputMethod = byInputMethod.getKey();
							String ability = byInputMethod.getValue().withCurrentModifier(modifier);
							if (ability != null) {
								if (renderAbilityName(key, inputMethod, available._inMoveset.get(ability), 
										x, y, color, guiGraphics, font, inContainerMenu)) {
									y += 9;
								}
							}
						}
					}
					guiGraphics.drawString(font, "(" + hotbar.switchAbilityKey.keyName().getString() + " to switch)", x, y, color);
				}
			}
		}
	}
	
	private static boolean renderAbilityName(ClientKeyWrapper key, InputMethod inputMethod, AbilityConditionCheck ability, 
			int x, int y, int color, GuiGraphics guiGraphics, Font font, boolean inContainerMenu) {
		if (ability != null && ability.ability != null) {
			AbilityInputActive mode = ability.ability.cl_IsInputActive();
			if (mode.showInHUD && (mode.inContainer == inContainerMenu)) {
				int nameColor = color;
				if (!ability.conditionCheck.isPositive()) {
					nameColor = ARGB.multiply(nameColor, 0xFF606060);
				}
				if (!mode.inputActive) {
					nameColor &= 0x40FFFFFF;
				}
				String bindName = inputMethod == InputMethod.HOLD ? "Hold " : "";
				String keyName = key.keyName().getString();
				bindName += keyName;
				guiGraphics.drawString(font, bindName + ": " + ability.ability.abilityId.nameInMoveset(), x, y, nameColor);
				return true;
			}
		}
		return false;
	}
	
	
}
