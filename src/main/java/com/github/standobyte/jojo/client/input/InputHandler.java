package com.github.standobyte.jojo.client.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;

import com.github.standobyte.jojo.client.event.PreKeyInputEvent;
import com.github.standobyte.jojo.client.jojomenu.IJojoMenuScreen;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.packet.fromclient.ClAbilityInputPacket;
import com.github.standobyte.jojo.core.packet.fromclient.ClSummonStandPacket;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityInputHandler;
import com.github.standobyte.jojo.powersystem.ability.AbilityInputHandler.ClickInputType;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.util.CommonEnums.DiagonalDirection2D;
import com.github.standobyte.jojo.util.CommonEnums.Direction2D;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

public class InputHandler {
	private static InputHandler instance;
	private final Minecraft mc = Minecraft.getInstance();
	
	public static void init(RegisterKeyMappingsEvent event) {
		if (instance == null) {
			instance = new InputHandler();
			instance.registerBindings(event);
			NeoForge.EVENT_BUS.register(instance);
		}
	}
	
	public static final String MAIN_CATEGORY = "key.categories.rotp";
	public KeyMapping summonStand;
	public KeyMapping jojoStuffMenu;
	public static final String HUD_CATEGORY = "key.categories.rotp.hud";
	public KeyMapping standHudMode;
	public KeyMapping playerPowerHudMode;
	
	private void registerBindings(RegisterKeyMappingsEvent event) {
		event.register(summonStand = new KeyMapping(
				"rotp.key.toggle_stand", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, MAIN_CATEGORY));
		event.register(jojoStuffMenu = new KeyMapping(
				"rotp.key.jojo_menu", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_BACKSLASH, MAIN_CATEGORY));
		event.register(standHudMode = new KeyMapping(
				"rotp.key.stand_mode", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, HUD_CATEGORY));
		event.register(playerPowerHudMode = new KeyMapping(
				"rotp.key.non_stand_mode", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, HUD_CATEGORY));
	}
	
	
	@SubscribeEvent
	public void handleKeyBindingsPost(ClientTickEvent.Post event) {
//		if (standHudMode.consumeClick()) {
//			actionsOverlay.switchMode(PowerClass.STAND);
//		}
//		
//		if (playerPowerHudMode.consumeClick()) {
//			actionsOverlay.switchMode(PowerClass.PLAYER_POWER);
//		}
//		
		if (summonStand.consumeClick()) {
//			if (standPower.hasPower() && !standPower.isActive()) {
//				actionsOverlay.onStandSummon();
//			}
			PacketDistributor.sendToServer(new ClSummonStandPacket());
		}
		
		if (jojoStuffMenu.consumeClick()) {
			IJojoMenuScreen.onScreenKeyPress();
		}
		
		for (var heldKey : heldKeys.values()) {
			heldKey.incTicks();
		}
	}
	
	@SubscribeEvent
	public void onFrameUpdate(RenderFrameEvent.Pre event) {
		float tickDelta = mc.getDeltaTracker().getRealtimeDeltaTicks();
		frameUpdateHeldKeys(tickDelta);
	}

	
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public void handleKeyInput(PreKeyInputEvent event) {
		if (mc.getConnection() == null) return;
		
		Key key = InputConstants.getKey(event.getKey(), event.getScanCode());
		if (input(keyId(key), key, key, event.getAction(), event.getModifiers())) {
			event.setCanceled(true);
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public void handleMouseInput(InputEvent.MouseButton.Pre event) {
		if (mc.getConnection() == null) return;
		
		Key key = InputConstants.Type.MOUSE.getOrCreate(event.getButton());
		if (input(keyId(key), key, key, event.getAction(), event.getModifiers())) {
			event.setCanceled(true);
		}
	}

	private Key lAlt = InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_LALT);
	private Key LMB = InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_LEFT);
	private Key RMB = InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_RIGHT);	
	private Key MMB = InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_MIDDLE);

	// XXX use separate input buffer on client
	private RegistryFriendlyByteBuf inputBuf;

	// TODO (!!!) input queue (make it possible to queue a barrage midway through a jab combo)
	/**
	 * Handles the direct events of keyboard/mouse inputs to trigger abilities from the player's moveset.
	 * @return true if the vanilla input should be cancelled.
	 */
	public boolean input(short keyId, Object key, @Nullable Key keyboardMouseKey, int inputType, int modifiers) {
		boolean cancelVanilla = false;
		heldAbility = null;
		clickAbility = null;
		
		Player player = mc.player;
		Power<?> power = player != null ? PowerClass.STAND.get(player) : null;
		
		switch (inputType) {
			case InputConstants.PRESS -> {
				// TODO ability HUD
				if (player == null || power == null || !power.hasPower() || !((StandPower) power).isSummoned()) return false;
				if (mc.screen != null || heldKeys.containsKey(lAlt)) return false;
				
				KeyModifier keyModifier = getCurModifier();
				
				resolveInputAbilitiesOnClick(power, key, keyboardMouseKey, keyModifier);
				boolean ambiguousClickOrHold = heldAbility != null && clickAbility != null;
				cancelVanilla = heldAbility != null || clickAbility != null;

				HeldKeyTimer heldKeyTimer = new HeldKeyTimer(keyId, cancelVanilla, keyModifier);
				if (ambiguousClickOrHold) {
					// TODO (!!!!) only do this if both abilities have a windup (if not, then idfk, it's 2AM rn)
					// also consider that the windup might be shorted than 4 ticks
					heldKeyTimer.clickHoldResolve = new ClickHoldResolve(power, heldAbility, clickAbility);
				}
				else {
					if (heldAbility != null) {
						doInput(ClickInputType.PRESS_HOLD, keyId, power, heldAbility, 0);
					}
					else if (clickAbility != null) {
						doInput(ClickInputType.PRESS_CLICK, keyId, power, clickAbility, 0);
					}
				}
				
				heldKeys.put(key, heldKeyTimer);
				addKeyModifier(keyboardMouseKey);
			}
			case InputConstants.RELEASE -> {
				HeldKeyTimer heldTicks = heldKeys.remove(key);
				
				if (heldTicks != null) {
					if (heldTicks.clickHoldResolve != null) {
						ClickHoldResolve keyResolution = heldTicks.clickHoldResolve;
						var wasItClick = keyResolution.keyReleased();
						if (wasItClick != null && wasItClick.input() == ClickHoldResolve.InputState.CLICK) {
							JojoMod.LOGGER.debug("aight, it is click (took {} ticks)", wasItClick.timeTook());
							doInput(ClickInputType.PRESS_CLICK, keyId, keyResolution.power, keyResolution.clickAbility, wasItClick.timeTook());
						}
					}
					else {
						doInput(ClickInputType.RELEASE, keyId, null, null, 0);
					}
				}
				
				removeKeyModifier(keyboardMouseKey);
			}
			case InputConstants.REPEAT -> {
				HeldKeyTimer heldKey = heldKeys.get(key);
				cancelVanilla = heldKey != null && heldKey.cancelVanilla;
			}
		}
		return cancelVanilla;
	}
	
	private <P extends Power<P>> void doInput(ClickInputType type, short keyId, Power<?> power, Ability<P> ability, float timeTookToResolve) {
		Player player = mc.player;
		switch (type) {
			case PRESS_CLICK -> {
				AbilityInputHandler.click(ability, power, inputBuf, timeTookToResolve);
				PacketDistributor.sendToServer(ClAbilityInputPacket.click(power, ability, timeTookToResolve));
			}
			case PRESS_HOLD -> {
				AbilityInputHandler.startHolding(keyId, ability, power, player, inputBuf, timeTookToResolve);
				PacketDistributor.sendToServer(ClAbilityInputPacket.startHold(keyId, power, ability, timeTookToResolve));
			}
			case RELEASE -> {
				AbilityInputHandler.releaseHolding(keyId, player);
				PacketDistributor.sendToServer(ClAbilityInputPacket.releaseHold(keyId));
			}
		}
	}
	
	public static short keyId(Key key) {
		return (short) ((key.getType().ordinal() & 3) | (key.getValue() << 2));
	}
	
	
	// Held keys stuff
	
	private Map<Object, HeldKeyTimer> heldKeys = new HashMap<>();
	
	public boolean isHeld(Object key, @Nullable KeyModifier modifier) {
		HeldKeyTimer timer = heldKeys.get(key);
		if (timer != null) {
			return modifier == null || timer.modifier == modifier;
		}
		return false;
	}
	
	private void frameUpdateHeldKeys(float tickDelta) {
		for (var timer : heldKeys.values()) {
			if (timer.clickHoldResolve != null) {
				var changedState = timer.clickHoldResolve.frameUpdate(tickDelta);
				if (changedState != null && changedState.input() == ClickHoldResolve.InputState.HOLD) {
					switch (changedState.input()) {
						// TODO (!!!!) only set the animation for the held ability action to the entity, but not the actual action yet
						case ASSUME_HOLD -> {}
						case HOLD -> {
							JojoMod.LOGGER.debug("aight, it is hold (took {} ticks)", changedState.timeTook());
							doInput(ClickInputType.PRESS_HOLD, timer.keyId, timer.clickHoldResolve.power, timer.clickHoldResolve.heldAbility, changedState.timeTook());
							timer.clickHoldResolve = null;
						}
						default -> {}
					}
				}
			}
		}
	}
	
	
	// Key modifiers (Ctrl/Shift/Alt)
	
	private List<KeyModifier> modifiersQueue = new ArrayList<>();
	
	private void addKeyModifier(Key key) {
		KeyModifier modifier = KeyModifier.getKeyModifier(key);
		if (modifier != KeyModifier.NONE && !modifiersQueue.contains(modifier)) {
			modifiersQueue.add(modifier);
		}
	}
	
	private void removeKeyModifier(Key key) {
		KeyModifier modifier = KeyModifier.getKeyModifier(key);
		if (modifier != KeyModifier.NONE) {
			modifiersQueue.remove(modifier);
		}
	}
	
	private KeyModifier getCurModifier() {
		return !modifiersQueue.isEmpty() ? modifiersQueue.get(modifiersQueue.size() - 1) : KeyModifier.NONE;
	}
	
	
	// The function that figures out what ability has the player inputed.
	
	private Ability<?> heldAbility;
	private Ability<?> clickAbility;
	
	private void resolveInputAbilitiesOnClick(Power<?> power, Object key, @Nullable Key keyboardMouseKey, KeyModifier keyModifier) {
		// XXX custom inputs
		if (key == LMB) {
			clickAbility = power.getMoveset().getAbility("punch");
			heldAbility = power.getMoveset().getAbility("barrage");
		}
		else if (key == RMB) {
			if (keyModifier == KeyModifier.CONTROL) {
				clickAbility = power.getMoveset().getAbility("grab");
				// grab_terrain
				// grabbed_release
				// grabbed_throw
			}
			else {
				clickAbility = power.getMoveset().getAbility("heavy_punch");
				heldAbility = power.getMoveset().getAbility("heavy_charged");
			}
		}
		else if (key == MMB) {
			DiagonalDirection2D dir = DiagonalDirection2D.fromDirs(
					isHeld(mc.options.keyUp.getKey(), null), 
					isHeld(mc.options.keyUp.getKey(), null), 
					isHeld(mc.options.keyUp.getKey(), null), 
					isHeld(mc.options.keyUp.getKey(), null));
			if (dir != null) {
				clickAbility = power.getMoveset().getAbility("quickstep");
//				quickstepDir = dir;
			}
			else {
				clickAbility = power.getMoveset().getAbility("dodge");
				heldAbility = power.getMoveset().getAbility("block");
			}
		}
		// XXX (quickstep) only trigger if MMB was used as quickstep when pressed
		// XXX (quickstep) diagonal quickstep motion when multiple of the WASD keys are pressed in the same tick (but i should also cancel them right here)
//		else if (isHeld(MMB, KeyModifier.CONTROL)) {
//			if (key == mc.options.keyUp.getKey()) {
//				clickAbility = power.getMoveset().getAbility("quickstep");
//				quickstepDir = DiagonalDirection2D.UP;
//			}
//			else if (key == mc.options.keyDown.getKey()) {
//				clickAbility = power.getMoveset().getAbility("quickstep");
//				quickstepDir = DiagonalDirection2D.DOWN;
//			}
//			else if (key == mc.options.keyLeft.getKey()) {
//				clickAbility = power.getMoveset().getAbility("quickstep");
//				quickstepDir = DiagonalDirection2D.LEFT;
//			}
//			else if (key == mc.options.keyRight.getKey()) {
//				clickAbility = power.getMoveset().getAbility("quickstep");
//				quickstepDir = DiagonalDirection2D.RIGHT;
//			}
//		}
		// manual_control
		// swap_items
		// item_lmb
		// item_rmb
		// leap
		// special
	}
	
	
//	public static DiagonalDirection2D quickstepDir;
	
	
	public static final Int2ObjectMap<Direction2D> ARROW_KEYS = Util.make(new Int2ObjectOpenHashMap<>(), map -> {
		map.put(GLFW.GLFW_KEY_LEFT,  Direction2D.LEFT);
		map.put(GLFW.GLFW_KEY_UP,	 Direction2D.UP);
		map.put(GLFW.GLFW_KEY_RIGHT, Direction2D.RIGHT);
		map.put(GLFW.GLFW_KEY_DOWN,  Direction2D.DOWN);
	});
	
	@Nullable
	public static Direction2D getArrowKey(int keyCode) {
		return ARROW_KEYS.get(keyCode);
	}
}
