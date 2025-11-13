package com.github.standobyte.jojo.client.input;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.mutable.MutableInt;
import org.lwjgl.glfw.GLFW;

import com.github.standobyte.jojo.client.ui.jojomenu.IJojoMenuScreen;
import com.github.standobyte.jojo.client.ui.jojomenu.JojoMenuTabs;
import com.github.standobyte.jojo.client.ui.jojomenu.Tab;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.packet.fromclient.ClNoParamsPacket;
import com.github.standobyte.jojo.core.packet.fromclient.ClNoParamsPacket.PacketType;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.network.PacketDistributor;

public class VanillaKeybinds {
	public static final String MAIN_CATEGORY = "key.categories." + JojoMod.MOD_ID;
	public KeyMapping summonStand;
	public KeyMapping standArmsOnlyHUD;
	public KeyMapping playerPowerHUD;
	public KeyMapping useAbility;
	public KeyMapping switchSpecial;
	public KeyMapping disableHUDControls;
	public KeyMapping jojoStuffMenu;
	
	public static VanillaKeybinds register(RegisterKeyMappingsEvent event) {
		VanillaKeybinds binds = new VanillaKeybinds();
		event.register(binds.summonStand = new Jokerge(
				JojoMod.MOD_ID + ".key.toggle_stand", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, MAIN_CATEGORY)
				.inInitOrder().withDescTooltip());
		event.register(binds.standArmsOnlyHUD = new Jokerge(
				JojoMod.MOD_ID + ".key.stand_mode", KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, MAIN_CATEGORY)
				.inInitOrder().withDescTooltip());
		event.register(binds.playerPowerHUD = new Jokerge(
				JojoMod.MOD_ID + ".key.non_stand_mode", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, MAIN_CATEGORY)
				.inInitOrder().withDescTooltip());
		event.register(binds.useAbility = new Jokerge(
				JojoMod.MOD_ID + ".key.use_special_ability", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, MAIN_CATEGORY)
				.inInitOrder().withDescTooltip());
		event.register(binds.switchSpecial = new Jokerge(
				JojoMod.MOD_ID + ".key.ability_hotbar", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, MAIN_CATEGORY)
				.inInitOrder().withDescTooltip());
		event.register(binds.disableHUDControls = new Jokerge(
				JojoMod.MOD_ID + ".key.disable_hotbars", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, MAIN_CATEGORY)
				.inInitOrder().withDescTooltip());
		event.register(binds.jojoStuffMenu = new Jokerge(
				JojoMod.MOD_ID + ".key.jojo_menu", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_BACKSLASH, MAIN_CATEGORY)
				.inInitOrder().withDescTooltip());
		return binds;
	}
	
	public static final Set<String> ADD_DESC_TOOLTIP = new HashSet<>();
	
	public static class Jokerge extends KeyMapping {
		protected static Map<String, MutableInt> PER_CATEGORY = new HashMap<String, MutableInt>();
		protected int orderIndex = Integer.MAX_VALUE;

		public Jokerge(String name, int keyCode, String category) {	super(name, keyCode, category); }
		public Jokerge(String name, InputConstants.Type type, int keyCode, String category) { super(name, type, keyCode, category); }
		public Jokerge(String description, IKeyConflictContext keyConflictContext, InputConstants.Type inputType, int keyCode, String category) { super(description, keyConflictContext, inputType, keyCode, category); }
		public Jokerge(String description, IKeyConflictContext keyConflictContext, InputConstants.Key keyCode, String category) { super(description, keyConflictContext, keyCode, category); }
		public Jokerge(String description, IKeyConflictContext keyConflictContext, KeyModifier keyModifier, InputConstants.Type inputType, int keyCode, String category) { super(description, keyConflictContext, keyModifier, inputType, keyCode, category); }
		public Jokerge(String description, IKeyConflictContext keyConflictContext, KeyModifier keyModifier, InputConstants.Key keyCode, String category) { super(description, keyConflictContext, keyModifier, keyCode, category); }
		
		public Jokerge inInitOrder() {
			this.orderIndex = PER_CATEGORY.computeIfAbsent(this.getCategory(), __ -> new MutableInt()).getAndIncrement();
			return this;
		}
		
		public Jokerge withDescTooltip() {
			ADD_DESC_TOOLTIP.add(this.getName());
			return this;
		}

		@Override
		public int compareTo(KeyMapping other) {
			if (this.getCategory() == other.getCategory() && other instanceof Jokerge jokerge) {
				int compare = Integer.compare(this.orderIndex, jokerge.orderIndex);
				if (compare != 0) return compare;
			}
			return super.compareTo(other);
		}
	}

	public void handleTick() {
		InputHandler inputHandler = InputHandler.getInstance();
		if (standArmsOnlyHUD.consumeClick()) {
			inputHandler.curPowerClassToggle = inputHandler.curPowerClassToggle != PowerClass.STAND ? PowerClass.STAND : null;
		}
		
		if (playerPowerHUD.consumeClick()) {
			inputHandler.curPowerClassToggle = inputHandler.curPowerClassToggle != PowerClass.PLAYER_POWER ? PowerClass.PLAYER_POWER : null;
		}
//		
		if (summonStand.consumeClick()) {
//			if (standPower.hasPower() && !standPower.isActive()) {
//				actionsOverlay.onStandSummon();
//			}
			PacketDistributor.sendToServer(ClNoParamsPacket.of(PacketType.SUMMON_STAND));
		}
		
		if (jojoStuffMenu.consumeClick()) {
			Minecraft mc = Minecraft.getInstance();
			if (mc.screen instanceof IJojoMenuScreen) {
				mc.popGuiLayer();
			}
			else {
				Tab tab = JojoMenuTabs.getTabToOpenOnMenuKey();
				if (tab != null) {
					tab.onClick(mc, mc.screen);
				}
			}
		}
	}
	
}
