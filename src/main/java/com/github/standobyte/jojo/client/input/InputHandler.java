package com.github.standobyte.jojo.client.input;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.event.PreKeyInputEvent;
import com.github.standobyte.jojo.client.input.controlscheme.ClientControlScheme;
import com.github.standobyte.jojo.client.input.controlscheme.ClientKeyWrapper;
import com.github.standobyte.jojo.client.input.controlscheme.AllControlSchemes;
import com.github.standobyte.jojo.core.packet.fromclient.ClAbilityInputPacket;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityInput;
import com.github.standobyte.jojo.powersystem.ability.AbilityInput.InputEventType;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities.AbilityConditionCheck;
import com.github.standobyte.jojo.powersystem.ability.condition.ConditionCheck;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPower;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.util.CommonEnums.Direction2D;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.player.ClientInput;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
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
	
	public static InputHandler getInstance() {
		return instance;
	}
	
	public VanillaKeybinds keybinds;
	
	private void registerBindings(RegisterKeyMappingsEvent event) {
		this.keybinds = VanillaKeybinds.register(event);
	}
	
	@Deprecated
	public static boolean holdingLAlt;
	public ClientKeyWrapper lAlt = ClientKeyWrapper.make(InputConstants.Type.KEYSYM, InputConstants.KEY_LALT);
	
	
	@SubscribeEvent
	public void handleKeyBindingsPost(ClientTickEvent.Post event) {
		keybinds.handleTick();
		for (var heldKey : heldKeys.values()) {
			heldKey.incTicks();
		}
		tickReleaseEventQueue();
	}
	
	@SubscribeEvent
	public void onFrameUpdate(RenderFrameEvent.Pre event) {
		holdingLAlt = heldKeys.containsKey(lAlt);
		float tickDelta = mc.getDeltaTracker().getRealtimeDeltaTicks();
		frameUpdateHeldKeys(tickDelta);
	}



	@SubscribeEvent(priority = EventPriority.LOW)
	public void handleKeyInput(PreKeyInputEvent event) {
		if (mc.getConnection() == null) return;

		InputConstants.Type keyType;
		int keyCode;
		if (event.getKey() == -1) {
			keyType = InputConstants.Type.SCANCODE;
			keyCode = event.getScanCode();
		}
		else {
			keyType = InputConstants.Type.KEYSYM;
			keyCode = event.getKey();
		}
		handleInputEvent(ClientKeyWrapper.make(keyType, keyCode), event.getAction(), event.getModifiers(), event);
	}
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public void handleMouseInput(InputEvent.MouseButton.Pre event) {
		if (mc.getConnection() == null) return;
		
		handleInputEvent(ClientKeyWrapper.make(InputConstants.Type.MOUSE, event.getButton()), event.getAction(), event.getModifiers(), event);
	}
	
	public void handleInputEvent(ClientKeyWrapper key, int action, int modifiers, ICancellableEvent event) {
		if (action == InputConstants.RELEASE && mc.screen instanceof ChatScreen) {
			keyReleaseEventQueue.add(new DelayedInput(key, action, modifiers));
		}
		else if (input(key, action, modifiers)) {
			event.setCanceled(true);
		}
	}
	
	protected void tickReleaseEventQueue() {
		if (!keyReleaseEventQueue.isEmpty() && mc.getConnection() != null && mc.screen == null) {
			for (DelayedInput keyRelease : keyReleaseEventQueue) {
				input(keyRelease.key, keyRelease.action, keyRelease.modifiers);
			}
			keyReleaseEventQueue.clear();
		}
	}
	
	public static record DelayedInput(ClientKeyWrapper key, int action, int modifiers) {}
	
	private Queue<DelayedInput> keyReleaseEventQueue = new ArrayDeque<>();
	
	
	private FriendlyByteBuf inputBuf = new FriendlyByteBuf(Unpooled.buffer());
	
	public Power<?> getCurPower() {
		if (mc.player != null) {
			StandPower standPower = ClientPowerCache.getPower(PowerClass.STAND);
			if (standPower != null && standPower.hasPower() && standPower.isSummoned()) {
				return standPower;
			}
			
			PlayerPower power = ClientPowerCache.getPower(PowerClass.PLAYER_POWER);
			if (power != null && power.hasPower()) {
				return power;
			}
		}
		return null;
	}
	
	public boolean input(ClientKeyWrapper key, int action, int modifiers) {
		return input(key.keyId(), key, action, modifiers);
	}

	/**
	 * Handles the direct events of keyboard/mouse inputs to trigger abilities from the player's moveset.
	 * @return true if the vanilla input should be cancelled.
	 */
	public boolean input(short keyId, ClientKeyWrapper key, int inputType, int modifiers) {
		boolean cancelVanilla = false;
		Power<?> power = getCurPower();
		
		switch (inputType) {
			case InputConstants.PRESS -> {
				// TODO ability HUD
				if (power == null) return false;
				
				KeyModifier keyModifier = getCurModifier();
				
				CurInput input = getInputAbilitiesOnClick(power, key, keyModifier);
				@Nullable Ability heldAbility = input.heldAbility != null ? input.heldAbility.ability : null;
				@Nullable Ability clickAbility = input.clickAbility != null ? input.clickAbility.ability : null;
				
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
						doInput(InputEventType.PRESS_HOLD, keyId, power, heldAbility, input.heldAbility.conditionCheck, 0);
					}
					else if (clickAbility != null) {
						doInput(InputEventType.PRESS_CLICK, keyId, power, clickAbility, input.clickAbility.conditionCheck, 0);
					}
				}
				
				heldKeys.put(key, heldKeyTimer);
				
				Key vanillaKey = key.getVanillaKey();
				if (vanillaKey != null) {
					addKeyModifier(vanillaKey);
				}
			}
			case InputConstants.RELEASE -> {
				HeldKeyTimer heldTicks = heldKeys.remove(key);
				
				if (heldTicks != null) {
					if (heldTicks.clickHoldResolve != null) {
						clickHeldOnRelease(heldTicks, keyId);
					}
					doInput(InputEventType.RELEASE, keyId, null, null, ConditionCheck.POSITIVE, 0);
				}

				Key vanillaKey = key.getVanillaKey();
				if (vanillaKey != null) {
					removeKeyModifier(vanillaKey);
				}
			}
			case InputConstants.REPEAT -> {
				HeldKeyTimer heldKey = heldKeys.get(key);
				cancelVanilla = heldKey != null && heldKey.cancelVanilla;
			}
		}
		return cancelVanilla;
	}
	
	private void doInput(InputEventType type, short keyId, Power<?> power, Ability ability, ConditionCheck conditionCheck, float timeTookToResolve) {
		Player player = mc.player;
		switch (type) {
			case PRESS_CLICK, PRESS_HOLD -> {
				if (ability == null || player == null) return;

				if (conditionCheck.isPositive()) {
					ability.writeExtraInput(inputBuf, player, true);
					AbilityInput.keyPress(keyId, ability, player, inputBuf, type.inputMethod, timeTookToResolve);
				}
				PacketDistributor.sendToServer(ClAbilityInputPacket.keyPress(keyId, player, power, ability, type, timeTookToResolve));
			}
			case RELEASE -> {
				AbilityInput.keyRelease(keyId, player);
				PacketDistributor.sendToServer(ClAbilityInputPacket.releaseHold(keyId));
			}
		}
	}
	
	@Deprecated
	public boolean inputsDisabled() {
		return mc.screen != null || heldKeys.containsKey(lAlt);
	}
	
	
	// Held keys stuff
	
	public Map<ClientKeyWrapper, HeldKeyTimer> heldKeys = new HashMap<>();
	
	public boolean isHeld(ClientKeyWrapper key, @Nullable KeyModifier modifier) {
		HeldKeyTimer timer = heldKeys.get(key);
		if (timer != null) {
			return modifier == null || timer.modifier == modifier;
		}
		return false;
	}
	
	private void clickHeldOnRelease(HeldKeyTimer heldTicks, short keyId) {
		ClickHoldResolve keyResolution = heldTicks.clickHoldResolve;
		var wasItClick = keyResolution.keyReleased();
		if (wasItClick != null && wasItClick.input() == ClickHoldResolve.InputState.CLICK) {
			Power<?> power = keyResolution.power;
			Ability ability = keyResolution.clickAbility;
			ConditionCheck conditionCheck = ClientPowerCache.getAvailableMoves(power.getPowerClass(), power).getConditionCheck(ability);
			float ticksToResolveClick = wasItClick.timeTook();
			doInput(InputEventType.PRESS_CLICK, keyId, power, ability, conditionCheck, ticksToResolveClick);
		}
	}
	
	private void frameUpdateHeldKeys(float tickDelta) {
		for (var timer : heldKeys.values()) {
			ClickHoldResolve keyResolution = timer.clickHoldResolve;
			if (keyResolution != null) {
				var changedState = keyResolution.frameUpdate(tickDelta);
				if (changedState != null) {
					switch (changedState.input()) {
						// TODO (!!!!) only set the animation for the held ability action to the entity, but not the actual action yet
						case ASSUME_HOLD -> {}
						case HOLD -> {
							Power<?> power = keyResolution.power;
							Ability ability = keyResolution.heldAbility;
							ConditionCheck conditionCheck = ClientPowerCache.getAvailableMoves(power.getPowerClass(), power).getConditionCheck(ability);
							float ticksToResolveHeld = changedState.timeTook();
							doInput(InputEventType.PRESS_HOLD, timer.keyId, power, ability, conditionCheck, ticksToResolveHeld);
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
	
	@Nonnull
	public KeyModifier getCurModifier() {
		return !modifiersQueue.isEmpty() ? modifiersQueue.get(modifiersQueue.size() - 1) : KeyModifier.NONE;
	}
	
	
	// The function that figures out what ability has the player inputed.
	
	private CurInput getInputAbilitiesOnClick(Power<?> power, ClientKeyWrapper key, KeyModifier keyModifier) {
		CurInput input = CurInput.instance;
		input.heldAbility = null;
		input.clickAbility = null;
		
		if (power.hasPower()) {
			ClientControlScheme controlScheme = AllControlSchemes.controls.get(power.getPowerType().getId());
			
			if (controlScheme != null) {
				List<String> heldBound = controlScheme.getBindsWithModifier(InputMethod.HOLD, key, keyModifier);
				List<String> clickBound = controlScheme.getBindsWithModifier(InputMethod.CLICK, key, keyModifier);
				
				if (!(heldBound.isEmpty() && clickBound.isEmpty())) {
					AvailableAbilities available = ClientPowerCache.getAvailableMoves(power.getPowerClass(), power);
	
					input.heldAbility = ClientControlScheme.prioritizedAbility(heldBound, available, true);
					input.clickAbility = ClientControlScheme.prioritizedAbility(clickBound, available, true);
				}
			}
		}
		
		return input;
	}
	
	static class CurInput {
		private static CurInput instance = new CurInput();
		
		public AbilityConditionCheck heldAbility;
		public AbilityConditionCheck clickAbility;
	}
	
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void playerMovementInput(MovementInputUpdateEvent event) {
		Player player = event.getEntity();
		ClientInput input = event.getInput();
		float movementMultiplier = 1;
		
		EntityActionInstance playerAction = LivingComponentAction.getCurEntityAction(player);
		if (playerAction != null) {
			movementMultiplier *= playerAction.userWalkSpeed;
		}
		
		StandEntity stand = ClientGlobals.playerStandEntity;
		if (stand != null) {
			EntityActionInstance standAction = LivingComponentAction.getCurEntityAction(stand);
			if (standAction != null) {
				movementMultiplier *= standAction.userWalkSpeed;
			}
		}
		
		if (movementMultiplier != 1) {
			input.forwardImpulse *= movementMultiplier;
			input.leftImpulse *= movementMultiplier;
			if (movementMultiplier < 1) {
				player.setSprinting(false);
			}
		}
	}
	
	
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
