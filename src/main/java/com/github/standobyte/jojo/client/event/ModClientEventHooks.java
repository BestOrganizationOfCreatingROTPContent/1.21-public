package com.github.standobyte.jojo.client.event;

import net.neoforged.neoforge.common.NeoForge;

public class ModClientEventHooks {

	public static boolean onKeyboardInputPre(int key, int scanCode, int action, int modifiers) {
		return NeoForge.EVENT_BUS.post(new PreKeyInputEvent(key, scanCode, action, modifiers)).isCanceled();
	}
}
