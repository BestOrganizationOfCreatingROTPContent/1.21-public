package com.github.standobyte.jojo;

import com.github.standobyte.jojo.client.input.InputHandler;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.settings.KeyModifier;

@EventBusSubscriber(value = Dist.CLIENT, bus = Bus.MOD)
public class DebugStandHud {

	@SubscribeEvent
	public static void addHud(RegisterGuiLayersEvent zaloopa) {
		zaloopa.registerAboveAll(JojoMod.resLoc("prototype_stand_hud"), new PrototypeStandHud());
	}
	
	public static class PrototypeStandHud implements LayeredDraw.Layer {

		@Override
		public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
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
			Ability ability;
			
			int color = 0xFFFFFFFF;
			if (power instanceof StandPower standPower) {
				StandSkin skin = StandSkinsLoader.getInstance().getSkin(standPower);
				if (skin != null) {
					color = skin.getColor();
				}
			}
			if (input.inputsDisabled()) {
				color &= 0x40FFFFFF;
			}
			
			ability = Ability.resolveSubAbility(input.getLMBClickAbility(power, modifier), player);
			if (ability != null) guiGraphics.drawString(font, "LMB: " + ability.abilityId.nameInMoveset(), x, y, color);
			y += 9;
			
			ability = Ability.resolveSubAbility(input.getLMBHeldAbility(power, modifier), player);
			if (ability != null) guiGraphics.drawString(font, "Hold LMB: " + ability.abilityId.nameInMoveset(), x, y, color);
			y += 9;
			
			ability = Ability.resolveSubAbility(input.getRMBClickAbility(power, modifier), player);
			if (ability != null) guiGraphics.drawString(font, "RMB: " + ability.abilityId.nameInMoveset(), x, y, color);
			y += 9;
			
			ability = Ability.resolveSubAbility(input.getRMBHeldAbility(power, modifier), player);
			if (ability != null) guiGraphics.drawString(font, "Hold RMB: " + ability.abilityId.nameInMoveset(), x, y, color);
			y += 9;
		}
		
	}
	
}
