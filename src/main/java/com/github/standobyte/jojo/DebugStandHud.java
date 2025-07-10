package com.github.standobyte.jojo;

import java.util.List;

import com.github.standobyte.jojo.client.input.ClientPowerCache;
import com.github.standobyte.jojo.client.input.ControlScheme;
import com.github.standobyte.jojo.client.input.ControlScheme.KeybindNoModifier;
import com.github.standobyte.jojo.client.input.InputHandler;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.power.ModPlayerPowers;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.Ability.AbilityInputActive;
import com.github.standobyte.jojo.powersystem.ability.AbilityInput.InputType;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.mojang.blaze3d.platform.InputConstants;

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
			
			if (player == null || power == null) return;
			
			KeyModifier modifier = input.getCurModifier();
			
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
			
			ControlScheme controlScheme = null;
			if (power.getPowerClass() == PowerClass.STAND) {
				controlScheme = ControlScheme.PROTOTYPE_STAND;
			}
			else if (power.getPowerClass() == PowerClass.PLAYER_POWER) {
				if (power.getPowerType() == ModPlayerPowers.HAMON.get()) {
					controlScheme = ControlScheme.PROTOTYPE_HAMON;
				}
			}
			
			if (controlScheme != null) {
				AvailableAbilities available = ClientPowerCache.getAvailableMoves(power.getPowerClass(), power);
				var binds = controlScheme.bindsMapView;
				for (var bindEntry : binds.entrySet()) {
					KeybindNoModifier keybind = bindEntry.getKey();
					List<String> boundAbilities = bindEntry.getValue().withCurrentModifier(modifier);
					var ability = ControlScheme.prioritizedAbility(boundAbilities, available, false);
					if (ability.ability != null) {
						AbilityInputActive mode = ability.ability.cl_IsInputActive();
						if (mode.showInHUD && (mode.inContainer == inContainerMenu)) {
							int nameColor = color;
							if (!ability.conditionCheck.isPositive()) {
								nameColor = ARGB.multiply(nameColor, 0xFF606060);
							}
							if (!mode.inputActive) {
								nameColor &= 0x40FFFFFF;
							}
							String bindName = keybind.inputType() == InputType.HOLD ? "Hold " : "";
							String keyName = ((InputConstants.Key) keybind.key()).getDisplayName().getString();
							bindName += keyName;
							guiGraphics.drawString(font, bindName + ": " + ability.ability.abilityId.nameInMoveset(), x, y, nameColor);
							y += 9;
						}
					}
				}
			}
		}
		
	}
	
	
}
