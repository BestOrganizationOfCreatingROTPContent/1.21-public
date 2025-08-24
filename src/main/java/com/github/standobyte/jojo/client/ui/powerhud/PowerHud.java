package com.github.standobyte.jojo.client.ui.powerhud;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.ClientPowerCache;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.input.AbilityInputState;
import com.github.standobyte.jojo.client.input.InputHandler;
import com.github.standobyte.jojo.client.input.controlscheme.AllControlSchemes;
import com.github.standobyte.jojo.client.input.controlscheme.ClientControlScheme;
import com.github.standobyte.jojo.client.input.controlscheme.ClientControlScheme.Hotbar;
import com.github.standobyte.jojo.client.input.controlscheme.ClientControlScheme.KeyModifierMap;
import com.github.standobyte.jojo.client.input.controlscheme.ClientKeyWrapper;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.client.text.ShortenText;
import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities.AbilityConditionCheck;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.v1_21_4_stuff.missingmethods.ARGB;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.NeoForge;

@EventBusSubscriber(value = Dist.CLIENT, bus = Bus.MOD)
public class PowerHud {
	
	public static PrototypeAbilityHud abilityHUDInstance;

	@SubscribeEvent
	public static void addHud(RegisterGuiLayersEvent event) {
		event.registerAboveAll(JojoMod.resLoc("ability_hud"), abilityHUDInstance = new PrototypeAbilityHud());
		NeoForge.EVENT_BUS.register(new GameBusEventHandler());
	}
	
	
	public static boolean hasElementTooltips(Screen screen) {
		return screen instanceof ChatScreen;
	}
	
	public static boolean canDragElementsOn(Screen screen) {
		return screen instanceof ChatScreen;
	}
	
	public static boolean isInContainerScreen() {
		return Minecraft.getInstance().screen instanceof AbstractContainerScreen;
	}
	
	
	public static class GameBusEventHandler {
		
		@SubscribeEvent
		public void onContainerMenuRender(ContainerScreenEvent.Render.Foreground event) {
			GuiGraphics graphics = event.getGuiGraphics();
			AbstractContainerScreen<?> screen = event.getContainerScreen();
			graphics.pose().pushPose();
			graphics.pose().translate(-screen.getGuiLeft(), -screen.getGuiTop(), 0.0F);
			abilityHUDInstance.setupRender(true);
			abilityHUDInstance.renderAbilitiesHUD(graphics, Minecraft.getInstance().getTimer()/*getDeltaTracker()*/);
			graphics.pose().popPose();
		}
		
		@SubscribeEvent
		public void addDraggableToScreen(ScreenEvent.Init.Post event) {
			Screen screen = event.getScreen();
			if (canDragElementsOn(screen)) {
				for (HudElement element : abilityHUDInstance.elements.values()) {
					event.addListener(element);
				}
			}
		}
	}
	
	
	public static class PrototypeAbilityHud implements LayeredDraw.Layer {
		public Map<String, HudElement> elements = new HashMap<>();
		
		public HudElement addElement(HudElement element) {
			element.hud = this;
			elements.put(element.name, element);
			return element;
		}
		
		
		private boolean inContainerMenu;
		private int mouseX;
		private int mouseY;
		
		public void setupRender(boolean inContainerMenu) {
			setupRender(inContainerMenu, -1, -1);
		}
		
		public void setupRender(boolean inContainerMenu, int mouseX, int mouseY) {
			this.inContainerMenu = inContainerMenu;
			this.mouseX = mouseX;
			this.mouseY = mouseY;
		}

	
		public HudElement controls = addElement(new Controls("controls", 12, 56, -1, -1));
		public HudElement resolveBar = addElement(new Resolve("resolve_bar", 12, 12, 29, 16));
		public HudElement staminaBar = addElement(new Stamina("stamina_bar", 61, 16, Bars.HORIZONTAL_LENGTH + 8, Bars.HORIZONTAL_WIDTH));
		public HudElement finisherBar = addElement(new Finisher("stand_finisher", 
				HudElement.SnappingH.CENTER, HudElement.SnappingV.CENTER, -16, -16, 32, 32));
		
		@Override
		public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
			Minecraft mc = Minecraft.getInstance();
			if (!(mc.screen instanceof AbstractContainerScreen)) {
				if (mc.screen != null && hasElementTooltips(mc.screen)) {
					int mouseX = (int)(mc.mouseHandler.xpos()
							* (double)mc.getWindow().getGuiScaledWidth()
							/ (double)mc.getWindow().getScreenWidth());
					int mouseY = (int)(mc.mouseHandler.ypos()
							* (double)mc.getWindow().getGuiScaledHeight()
							/ (double)mc.getWindow().getScreenHeight());
					setupRender(false, mouseX, mouseY);
				}
				else {
					setupRender(false);
				}
				renderAbilitiesHUD(guiGraphics, deltaTracker);
			}
		}

		public void renderAbilitiesHUD(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
			Minecraft mc = Minecraft.getInstance();
			if (mc.options.hideGui) return;
			
			for (var element : elements.values()) {
				if (element.shouldRender()) {
					RenderSystem.enableBlend();
					RenderSystem.defaultBlendFunc();
					if (mouseX > -1 && mouseY > -1) {
						element.render(guiGraphics, deltaTracker, mouseX, mouseY);
					}
					else {
						element.render(guiGraphics, deltaTracker);
					}
				}
			}
			RenderSystem.disableBlend();
		}
		
	}
	
	
		
		
		
	public static class Controls extends HudElement {

		public Controls(String name, int x0, int y0, int width, int height) { super(name, x0, y0, width, height); }
		public Controls(String name, SnappingH snappingHorizontal, SnappingV snappingVertical, int xOffset, int yOffset, int width, int height) { super(name, snappingHorizontal, snappingVertical, xOffset, yOffset, width, height); }

		@Override
		public boolean shouldRender() {
			InputHandler input = InputHandler.getInstance();
			if (input == null) return false;
			
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null) return false;
			
			Power<?> power = input.getCurPower();
			if (power == null || !power.hasPower()) return false;
			
			return true;
		}
		
		@Override
		public void renderElement(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
			Minecraft mc = Minecraft.getInstance();
			InputHandler input = InputHandler.getInstance();
			Power<?> power = input.getCurPower();
			
			int x = this.getX();
			int yStarting = this.getY();
			int y = yStarting;
			Font font = mc.font;
			
			int color = 0xFFFFFFFF;
			if (power instanceof StandPower standPower) {
				StandSkin skin = StandSkinsLoader.getInstance().getSkin(standPower);
				if (skin != null) {
					color = skin.getColor();
				}
			}
			
			int lineWidth;
			int maxWidth = 0;

			ClientControlScheme controlScheme = AllControlSchemes.getForPowerType(power.getPowerType());
			if (controlScheme != null) {
				ClientControlScheme.MoveGroup curGroup = controlScheme.getCurGroup().getValue();
				@Nonnull KeyModifier modifier = input.getCurModifier();
				if (modifier == KeyModifier.ALT) modifier = KeyModifier.NONE;
				AvailableAbilities availableAbilities = ClientPowerCache.getAvailableMoves(power.getPowerClass(), power);
				
				Map<ClientKeyWrapper, Map<InputMethod, KeyModifierMap<List<String>>>> binds = curGroup.binds;
				// render separate keybinds
				for (var bindEntry : binds.entrySet()) {
					ClientKeyWrapper key = bindEntry.getKey();
					for (var byInputMethod : bindEntry.getValue().entrySet()) {
						InputMethod inputMethod = byInputMethod.getKey();
						KeyModifierMap<List<String>> bindsForInputMethod = byInputMethod.getValue();
						lineWidth = renderBind(inputMethod, bindsForInputMethod, power, 
								modifier, availableAbilities, key, false, 
								x, y, color, guiGraphics, font, hud.inContainerMenu);
						if (lineWidth >= 0) {
							maxWidth = Math.max(lineWidth, maxWidth);
							y += 9;
						}
						else if (modifier == KeyModifier.NONE) {
							// if you aren't pressing Ctrl or Shift, but there is no ability keybind to render, render the Ctrl and Shift ones instead
							for (KeyModifier otherModifier : KeyModifier.values()) {
								if (otherModifier != modifier) {
									lineWidth = renderBind(inputMethod, bindsForInputMethod, power, 
											otherModifier, availableAbilities, key, true, 
											x, y, color, guiGraphics, font, hud.inContainerMenu);
									if (lineWidth >= 0) {
										maxWidth = Math.max(lineWidth, maxWidth);
										y += 9;
									}
								}
							}
						}
					}
				}
				
				for (Hotbar hotbar : curGroup.hotbars) {
					y += 5;
					if (!hotbar.slots.isEmpty()) {
						ClientKeyWrapper key = hotbar.useAbilityKey;
						List<ClientControlScheme.HotbarSlot> slots = hotbar.slots;
						ClientControlScheme.HotbarSlot slot = slots.get(hotbar.slotIndex < slots.size() ? hotbar.slotIndex : 0);
						for (var byInputMethod : slot.binds.entrySet()) {
							InputMethod inputMethod = byInputMethod.getKey();
							String ability = byInputMethod.getValue().get(modifier);
							if (ability != null) {
								lineWidth = renderAbilityName(key, null, 
										inputMethod, availableAbilities._inMoveset.get(ability), power, 
										x, y, color, guiGraphics, font, hud.inContainerMenu);
								if (lineWidth >= 0) {
									maxWidth = Math.max(lineWidth, maxWidth);
									y += 9;
								}
							}
						}
					}
					String line = "(" + getKeyName(hotbar.switchAbilityKey) + " to switch)";
					guiGraphics.drawString(font, line, x, y, color);
					maxWidth = Math.max(font.width(line), maxWidth);
					y += 9;
				}
			}
			
			this.setSize(maxWidth, y - yStarting);
		}

		/**
		 * @return The text line width, or -1 if it wasn't rendered.
		 */
		private static int renderBind(InputMethod inputMethod, KeyModifierMap<List<String>> binds, Power<?> abilityCtx, 
				@Nonnull KeyModifier modifier, AvailableAbilities available, ClientKeyWrapper key, boolean withModifierName, 
				int x, int y, int color, GuiGraphics guiGraphics, Font font, boolean inContainerMenu) {
			List<String> boundAbilities = binds.get(modifier);
			if (boundAbilities.isEmpty()) {
				return -1;
			}
			AbilityConditionCheck ability = ClientControlScheme.prioritizedAbility(boundAbilities, available, abilityCtx, false);
			return renderAbilityName(key, withModifierName ? modifier : null, inputMethod, ability, abilityCtx, x, y, color, guiGraphics, font, inContainerMenu);
		}
		
		/**
		 * @return The text line width, or -1 if it wasn't rendered.
		 */
		private static int renderAbilityName(ClientKeyWrapper key, @Nullable KeyModifier modifier, 
				InputMethod inputMethod, AbilityConditionCheck ability, Power<?> abilityCtx, 
				int x, int y, int color, GuiGraphics guiGraphics, Font font, boolean inContainerMenu) {
			if (ability != null && ability.ability != null) {
				AbilityInputState state = AbilityInputState.withValue(ability.clientInputState);
				
				boolean showAbility = state.getFlag(AbilityInputState.IS_ACTIVE)
						|| state.getFlag(AbilityInputState.VISIBLE_EVEN_INACTIVE)
						|| state.getFlag(AbilityInputState.VISIBLE_TRANSLUCENT);
				showAbility &= state.getFlag(AbilityInputState.ONLY_IN_CONTAINER) == inContainerMenu;
				
				if (showAbility) {
					int nameColor = color;
					if (!ability.conditionCheck.isPositive()) {
						nameColor = ARGB32.multiply(nameColor, 0xFF606060);
					}
					if (state.getFlag(AbilityInputState.VISIBLE_TRANSLUCENT)) {
						nameColor &= 0x40FFFFFF;
					}
					
					String bindName = inputMethod == InputMethod.HOLD ? "Hold " : "";
					String keyName = getKeyName(key);
					bindName += keyName;
					if (modifier != null) {
						String modifierName = switch (modifier) {
							case CONTROL -> "Ctrl";
							case SHIFT -> "Shift";
							case ALT -> "Alt";
							default -> "";
						};
						if (!modifierName.isEmpty()) {
							bindName = modifierName + "+" + bindName;
						}
					}
					
					// TODO (!) translatable names
					String line = bindName + ": " + ability.ability.abilityId.nameInMoveset();
					int width = font.width(line);
					guiGraphics.drawString(font, line, x, y, nameColor);
					return width;
				}
			}
			return -1;
		}
		
		public static String getKeyName(ClientKeyWrapper key) {
//			if (key == ClientKeyWrapper.make(InputConstants.Type.MOUSE, InputConstants.MOUSE_BUTTON_LEFT)) {
//				return Character.toString(IconSymbols.LMB_CLICK);
//			}
//			else if (key == ClientKeyWrapper.make(InputConstants.Type.MOUSE, InputConstants.MOUSE_BUTTON_RIGHT)) {
//				return Character.toString(IconSymbols.RMB_CLICK);
//			}
//			else if (key == ClientKeyWrapper.make(InputConstants.Type.MOUSE, InputConstants.MOUSE_BUTTON_MIDDLE)) {
//				return Character.toString(IconSymbols.MMB_CLICK);
//			}
//			else {
				return ShortenText.shortenIfAble(key.keyName()).getString();
//			}
		}
		
	}
	
	
	public static class Resolve extends HudElement {
		public static final ResourceLocation HORIZONTAL_EMPTY = JojoMod.resLoc("textures/hud/stand_resolve_horizontal_empty.png");
		public static final ResourceLocation HORIZONTAL_FULL = JojoMod.resLoc("textures/hud/stand_resolve_horizontal_full.png");
		public static final ResourceLocation VERTICAL_EMPTY = JojoMod.resLoc("textures/hud/stand_resolve_vertical_empty.png");
		public static final ResourceLocation VERTICAL_FULL = JojoMod.resLoc("textures/hud/stand_resolve_vertical_full.png");

		public Resolve(String name, int x0, int y0, int width, int height) {
			super(name, x0, y0, width, height);
		}

		public Resolve(String name, SnappingH snappingHorizontal, SnappingV snappingVertical, 
				int xOffset, int yOffset, int width, int height) {
			super(name, snappingHorizontal, snappingVertical, xOffset, yOffset, width, height);
		}

		@Override
		public boolean shouldRender() {
			StandPower standPower = ClientPowerCache.getPower(PowerClass.STAND);
			return standPower != null && standPower.usesResolve();
		}
		
		@Override
		public void renderElement(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
			StandPower standPower = ClientPowerCache.getPower(PowerClass.STAND);
			float resolveRatio = standPower.getResolveRatio();
			int x = getX();
			int y = getY();
			int width = getWidth();
			int height = getHeight();
			Minecraft mc = Minecraft.getInstance();
			BlitFloat.blit(guiGraphics.pose(), mc, HORIZONTAL_EMPTY, 
					x, y, width, height, 0, 
					BlitFloat.NO_TINT);
			float fillWidth = resolveRatio >= 1 ? width : Math.min(width * resolveRatio, width - 2);
			BlitFloat.blit(guiGraphics.pose(), mc, HORIZONTAL_FULL, 
					x, y, fillWidth, height, 0, 
					0, 0, fillWidth, height, width, height, 
					BlitFloat.NO_TINT);
		}
	}
	
	
	public static class Stamina extends HudElement {
		public static final ResourceLocation ICON = JojoMod.resLoc("textures/hud/stand_stamina.png");
		public static final ResourceLocation BAR_HORIZONTAL_FILL = JojoMod.resLoc("textures/hud/bars/bar_horizontal_stamina.png");
		public static final ResourceLocation BAR_HORIZONTAL_MINI_FILL = JojoMod.resLoc("textures/hud/bars/bar_horizontal_mini_stamina.png");
		public static final ResourceLocation BAR_VERTICAL_FILL = JojoMod.resLoc("textures/hud/bars/bar_vertical_stamina.png");
		public static final ResourceLocation BAR_VERTICAL_MINI_FILL = JojoMod.resLoc("textures/hud/bars/bar_vertical_mini_stamina.png");

		public Stamina(String name, int x0, int y0, int width, int height) {
			super(name, x0, y0, width, height);
		}

		public Stamina(String name, SnappingH snappingHorizontal, SnappingV snappingVertical, 
				int xOffset, int yOffset, int width, int height) {
			super(name, snappingHorizontal, snappingVertical, xOffset, yOffset, width, height);
		}

		@Override
		public boolean shouldRender() {
			StandPower standPower = ClientPowerCache.getPower(PowerClass.STAND);
			return standPower != null && standPower.usesStamina();
		}
		
		@Override
		public void renderElement(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
			StandPower standPower = ClientPowerCache.getPower(PowerClass.STAND);
			float staminaRatio = standPower.getStaminaRatio();
			int x = getX() + 8;
			int y = getY();
			Bars.renderHorizontalBar(guiGraphics.pose(), x, y, staminaRatio, BAR_HORIZONTAL_FILL, BlitFloat.NO_TINT);
			BlitFloat.blit(guiGraphics.pose(), Minecraft.getInstance(), ICON, 
					x - 12, y - 6, 20, 20, 0, BlitFloat.NO_TINT);
		}
	}
	
	
	public static class Finisher extends HudElement {
		public static final ResourceLocation[] BARS = {
				JojoMod.resLoc("textures/hud/stand_finisher_1.png"),
				JojoMod.resLoc("textures/hud/stand_finisher_2.png"),
				JojoMod.resLoc("textures/hud/stand_finisher_3.png")
		};

		public Finisher(String name, int x0, int y0, int width, int height) {
			super(name, x0, y0, width, height);
		}

		public Finisher(String name, SnappingH snappingHorizontal, SnappingV snappingVertical, 
				int xOffset, int yOffset, int width, int height) {
			super(name, snappingHorizontal, snappingVertical, xOffset, yOffset, width, height);
		}

		@Override
		public boolean shouldRender() {
			StandEntity stand = ClientGlobals.playerStandEntity;
			return stand != null && stand.getFinisherMeter() > 0;
		}
		
		@Override
		public void renderElement(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
			Minecraft mc = Minecraft.getInstance();
			StandEntity stand = ClientGlobals.playerStandEntity;
			float partialTick = ClientUtil.partialTick(deltaTracker, false);
			float finisher = stand.getFinisherMeter(partialTick);
			float x = getX();// + 0.5f;
			float y = getY();// + 0.5f;
			float width = getWidth();// - 1;
			float height = getHeight();// - 1;
			int i = 0;
			ResourceLocation bar;
			int color = ARGB.white(0.5f);
			
			while (finisher > 0 && i < BARS.length) {
				bar = BARS[i];
				if (finisher >= 1) {
					BlitFloat.blit(guiGraphics.pose(), mc, bar, 
							x, y, width, height, 0, 
							color);
				}
				else {
					BlitFloat.blitRadial(guiGraphics.pose(), mc, bar, 
							x, y, width, height, 0, 
							0, finisher, color);
				}
				
				finisher -= 1;
				i++;
			}
		}
	}
	
}
