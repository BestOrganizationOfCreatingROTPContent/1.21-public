package com.github.standobyte.jojo.client.ui;

import com.github.standobyte.jojo.core.JojoMod;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

// FIXME (wasd screen) mouse wheel scroll
// FIXME (wasd screen) when holding Shift/Ctrl, the keys get released and don't work
// fun fact: did you know that if you're holding W/S and X at the same time, you cannot jump?
@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public interface ScreenLetsUseWASD {

	public static boolean canUseWhenOpen(Screen screen) {
		return screen instanceof ScreenLetsUseWASD;
	}

	@SubscribeEvent
	public static void onKeyEvent(InputEvent.Key event) {
		Minecraft mc = Minecraft.getInstance();
		if (ScreenLetsUseWASD.canUseWhenOpen(mc.screen)) {
			InputConstants.Key key = InputConstants.getKey(event.getKey(), event.getScanCode());
			switch (event.getAction()) {
				case InputConstants.PRESS -> {
					KeyMapping.set(key, true);
					KeyMapping.click(key);
				}
				case InputConstants.RELEASE -> {
					KeyMapping.set(key, false);
				}
			}
		}
	}
}
