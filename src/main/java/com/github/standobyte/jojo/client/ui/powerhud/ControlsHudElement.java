package com.github.standobyte.jojo.client.ui.powerhud;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientPowerCache;
import com.github.standobyte.jojo.client.input.AbilityInputState;
import com.github.standobyte.jojo.client.input.InputHandler;
import com.github.standobyte.jojo.client.input.controlscheme.AllControlSchemes;
import com.github.standobyte.jojo.client.input.controlscheme.ClientControlScheme;
import com.github.standobyte.jojo.client.input.controlscheme.ClientControlScheme.Hotbar;
import com.github.standobyte.jojo.client.input.controlscheme.ClientControlScheme.HotbarSlot;
import com.github.standobyte.jojo.client.input.controlscheme.ClientControlScheme.KeyModifierMap;
import com.github.standobyte.jojo.client.input.controlscheme.ClientControlScheme.PowerClassAbility;
import com.github.standobyte.jojo.client.input.controlscheme.ClientKeyWrapper;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.client.standskin.sprites.AbilityIconSprites;
import com.github.standobyte.jojo.client.text.IconSymbols;
import com.github.standobyte.jojo.client.text.ShortenText;
import com.github.standobyte.jojo.client.ui.powerhud.PowerHud.PrototypeAbilityHud;
import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.client.ui.utils.TextUtil;
import com.github.standobyte.jojo.client.ui.utils.tooltip.TooltipParams;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities.AbilityConditionCheck;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;

import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.BelowOrAboveWidgetTooltipPositioner;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.util.TriState;

public class ControlsHudElement extends HudElement {
	public static final ResourceLocation HOTBARS_TEX = JojoMod.resLoc("textures/gui/overlay_hotbar.png");
	public static final GuiIcon[] HOTBARS = new GuiIcon[] {
			new GuiIcon(HOTBARS_TEX, 390,  50,  50, 50, 512, 512),
			new GuiIcon(HOTBARS_TEX, 370, 100,  70, 50, 512, 512),
			new GuiIcon(HOTBARS_TEX, 350, 150,  90, 50, 512, 512),
			new GuiIcon(HOTBARS_TEX, 330, 200, 110, 50, 512, 512),
			new GuiIcon(HOTBARS_TEX, 310, 250, 130, 50, 512, 512),
			new GuiIcon(HOTBARS_TEX, 290, 300, 150, 50, 512, 512),
			new GuiIcon(HOTBARS_TEX, 270, 350, 170, 50, 512, 512),
			new GuiIcon(HOTBARS_TEX, 250, 400, 190, 50, 512, 512),
			new GuiIcon(HOTBARS_TEX, 230, 450, 210, 50, 512, 512),
			new GuiIcon(HOTBARS_TEX,   0, 450, 230, 50, 512, 512),
			new GuiIcon(HOTBARS_TEX,   0, 400, 250, 50, 512, 512),
			new GuiIcon(HOTBARS_TEX,   0, 350, 270, 50, 512, 512),
			new GuiIcon(HOTBARS_TEX,   0, 300, 290, 50, 512, 512),
			new GuiIcon(HOTBARS_TEX,   0, 250, 310, 50, 512, 512),
			new GuiIcon(HOTBARS_TEX,   0, 200, 340, 50, 512, 512),
			new GuiIcon(HOTBARS_TEX,   0, 150, 350, 50, 512, 512),
			new GuiIcon(HOTBARS_TEX,   0, 100, 370, 50, 512, 512),
			new GuiIcon(HOTBARS_TEX,   0,  50, 390, 50, 512, 512),
			new GuiIcon(HOTBARS_TEX,   0,   0, 410, 50, 512, 512)
	};
	public static final GuiIcon HOTBAR_SELECTION = new GuiIcon(HOTBARS_TEX, 450, 10, 52, 52, 512, 512);

	public ControlsHudElement(String name, int x0, int y0, int width, int height) { super(name, x0, y0, width, height); }
	public ControlsHudElement(String name, SnappingH snappingHorizontal, SnappingV snappingVertical, int xOffset, int yOffset, int width, int height) { super(name, snappingHorizontal, snappingVertical, xOffset, yOffset, width, height); }

	@Override
	public boolean shouldRender() {
		InputHandler input = InputHandler.getInstance();
		if (input == null) return false;

		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return false;

		Power<?> power = input.getCurPower();
		if (power == null || !power.hasPower()) return false;

		return !hud.inContainerMenu.isFalse();
	}

	@Override
	public void renderElement(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		clear();

		Minecraft mc = Minecraft.getInstance();
		InputHandler input = InputHandler.getInstance();
		Power<?> power = input.getCurPower();
		Font font = mc.font;
		ClientControlScheme controlScheme = AllControlSchemes.getForPowerType(power.getPowerType());

		int color = 0xFFFFFFFF;
		@Nullable StandSkin standSkin = null;
		if (power instanceof StandPower standPower) {
			standSkin = StandSkinsLoader.getInstance().getSkin(standPower);
			if (standSkin != null) {
				color = standSkin.getColor();
			}
		}

		this.prepare(hud, controlScheme, font, input.getCurModifier(), power, standSkin);
		// TODO (controls HUD) update size
		this.renderControls(this.getX(), this.getY(), guiGraphics, deltaTracker, font, color);
	}
	
	@Override
	protected void checkTooltip(double mouseX, double mouseY, DeltaTracker deltaTracker) {
		InputHandler input = InputHandler.getInstance();
		Power<?> power = input.getCurPower();
		
		for (var abilitySlot : hoverableAbilities) {
			BindUI slot = abilitySlot.getFirst();
			ScreenRectangle rectangle = abilitySlot.getSecond();
			boolean isHovered = rectangle.containsPoint((int) mouseX, (int) mouseY);
			if (isHovered) {
				Screen screen = Minecraft.getInstance().screen;
				if (screen != null) {
					List<Component> tooltip = new ArrayList<>();
					for (var byInputMethod : slot.abilities.entrySet()) {
						InputMethod inputMethod = byInputMethod.getKey();
						AbilityBindUI ability = byInputMethod.getValue();
						Component keyName = switch (inputMethod) {
							case CLICK -> slot.keybind;
							case HOLD -> Component.translatable("ripples_hud.hold_key", slot.keybind);
						};
						Component line = Component.translatable("ripples_hud.key_ability", keyName, ability.ability.ability/*:fire::writing_hand:*/.getName(power))
								.withStyle(ChatFormatting.BLACK);
						tooltip.add(line);
					}
					screen.setTooltipForNextRenderPass(TextUtil.splitMultiLine(Minecraft.getInstance().font, 
							tooltip, TextUtil.TOOLTIP_MAX_WIDTH), new BelowOrAboveWidgetTooltipPositioner(rectangle), true);
					TooltipParams.set(TooltipParams.paperStyle());
				}
				break;
			}
		}
	}


	

	public static final int SLOT_WIDTH = 20;
	public static final int SLOT_HEIGHT = 20;
	
	public static class AbilityBindUI {
		public AbilityConditionCheck ability;
		public TextureAtlasSprite sprite;
		public Component keybind;
		public Component keybindAbilityName;
	}
	
	public static class BindUI {
		public Component keybind;
		public Map<InputMethod, AbilityBindUI> abilities = new EnumMap<>(InputMethod.class);
		
		public int y;
		public int keybindWidth;
		public int width;
	}

	public static class HotbarUILine {
		public List<HotbarSlotUI> slots = new ArrayList<>();
		public HotbarSlotUI selected;
		public Component keybind;
		public Component switchHint;

		public int y;
		public int keybindWidth;
		public int width;
	}
	
	public static class HotbarSlotUI {
		public BindUI bind;
		public AbilityBindUI sprite;
		public Map<InputMethod, AbilityBindUI> abilities = new EnumMap<>(InputMethod.class);
	}


	public List<BindUI> binds = new ArrayList<>();
	public List<HotbarUILine> hotbars = new ArrayList<>();
	public List<Pair<BindUI, ScreenRectangle>> hoverableAbilities = new ArrayList<>();

	public void clear() {
		binds.clear();
		hotbars.clear();
		hoverableAbilities.clear();
	}

	public void prepare(PrototypeAbilityHud hud, ClientControlScheme controlScheme, Font font, 
			@Nonnull KeyModifier modifier, Power<?> power, @Nullable StandSkin standSkin) {
		ClientControlScheme.MoveGroup curGroup = controlScheme.getCurGroup().getValue();
		if (modifier == KeyModifier.ALT) modifier = KeyModifier.NONE;
		AvailableAbilities availableAbilities = ClientPowerCache.getAvailableMoves(power.getPowerClass(), power);
		AbilityIconSprites abilityIconSprites = StandSkinsLoader.getInstance().abilityIcons;

		// separate keybinds
		Map<ClientKeyWrapper, Map<InputMethod, KeyModifierMap>> binds = curGroup.binds;
		for (var bindEntry : binds.entrySet()) {
			ClientKeyWrapper key = bindEntry.getKey();
			BindUI bindUI = new BindUI();
			for (var byInputMethod : bindEntry.getValue().entrySet()) {
				InputMethod inputMethod = byInputMethod.getKey();
				KeyModifierMap bindsForInputMethod = byInputMethod.getValue();

				AbilityBindUI abilityBindUI = makeBindUI(inputMethod, bindsForInputMethod, power, 
						modifier, availableAbilities, key, false, 
						abilityIconSprites, standSkin, 
						font, hud.inContainerMenu);

				if (abilityBindUI != null && modifier == KeyModifier.NONE) {
					// if you aren't pressing Ctrl or Shift, but there is no ability keybind to render, render the Ctrl and Shift ones instead
					for (KeyModifier otherModifier : KeyModifier.values()) {
						if (otherModifier != modifier) {
							abilityBindUI = makeBindUI(inputMethod, bindsForInputMethod, power, 
									otherModifier, availableAbilities, key, true, 
									abilityIconSprites, standSkin, 
									font, hud.inContainerMenu);
						}
					}
				}

				if (abilityBindUI != null) {
					bindUI.abilities.put(inputMethod, abilityBindUI);
				}
			}
			if (!bindUI.abilities.isEmpty()) {
				bindUI.keybind = getKeyName(key);
				bindUI.keybindWidth = font.width(bindUI.keybind) + 4;
				bindUI.width = bindUI.keybindWidth + bindUI.abilities.size() * SLOT_WIDTH + 4;
				this.binds.add(bindUI);
			}
		}

		// hotbars
		for (Hotbar hotbar : curGroup.hotbars) {
			if (!hotbar.slots.isEmpty()) {
				HotbarUILine hotbarUI = new HotbarUILine();

				ClientKeyWrapper key = hotbar.useAbilityKey;
				HotbarSlot selectedSlot = hotbar.getSelected();

				for (ClientControlScheme.HotbarSlot slot : hotbar.slots) {
					HotbarSlotUI slotUI = new HotbarSlotUI();
					slotUI.bind = new BindUI();
					slotUI.bind.keybind = getKeyName(key);

					for (var byInputMethod : slot.binds.entrySet()) {
						InputMethod inputMethod = byInputMethod.getKey();
						PowerClassAbility ability = byInputMethod.getValue().getFirst(modifier);
						if (ability != null) {
							AbilityBindUI bind = makeAbilityBindUI(key, null, 
									inputMethod, availableAbilities._inMoveset.get(ability.abilityName()), power, 
									abilityIconSprites, standSkin, 
									font, hud.inContainerMenu);
							if (bind != null) {
								if (slotUI.sprite == null || inputMethod == InputMethod.HOLD && InputHandler.getInstance().isHeld(key, modifier)) {
									slotUI.sprite = bind;
								}
								slotUI.abilities.put(inputMethod, bind);
								slotUI.bind.abilities.put(inputMethod, bind);
							}
						}
					}
					if (!slotUI.abilities.isEmpty()) {
						hotbarUI.slots.add(slotUI);
						if (slot == selectedSlot) {
							hotbarUI.selected = slotUI;
						}
					}
				}

				hotbarUI.keybind = getKeyName(key);
				hotbarUI.switchHint = Component.translatable("ripples_hud.hotbar_switch", getKeyName(hotbar.switchAbilityKey));
				hotbarUI.keybindWidth = font.width(hotbarUI.keybind) + 4;
				hotbarUI.width = hotbarUI.keybindWidth + hotbarUI.slots.size() * SLOT_WIDTH + 4;
				hotbarUI.width = Math.max(font.width(hotbarUI.switchHint), hotbarUI.width);

				this.hotbars.add(hotbarUI);
			}
		}


		int maxKeybindWidth = this.binds.stream().mapToInt(bind -> bind.keybindWidth).max().orElse(0);
		for (BindUI bindUI : this.binds) {
			bindUI.keybindWidth = maxKeybindWidth;
		}

		int maxHotbarKeybindWidth = this.hotbars.stream().mapToInt(hotbar -> hotbar.keybindWidth).max().orElse(0);
		for (var hotbarUI : this.hotbars) {
			hotbarUI.keybindWidth = maxHotbarKeybindWidth;
		}

		int y = 0;
		for (BindUI bind : this.binds) {
			bind.y = y;
			hoverableAbilities.add(Pair.of(bind, new ScreenRectangle(
					this.getX() + bind.keybindWidth, 
					this.getY() + y, 
					bind.abilities.size() * SLOT_WIDTH + 2, SLOT_HEIGHT + 2)));
			y += SLOT_HEIGHT + 2;
		}
		for (HotbarUILine hotbar : this.hotbars) {
			y += 8;
			hotbar.y = y;
			int x = 12;
			for (HotbarSlotUI slot : hotbar.slots) {
				if (slot.bind != null) {
					hoverableAbilities.add(Pair.of(slot.bind, new ScreenRectangle(
							this.getX() + x, 
							this.getY() + y, 
							SLOT_WIDTH + 2, SLOT_HEIGHT + 2)));
				}
				x += SLOT_WIDTH;
			}
			y += SLOT_HEIGHT + 2;
			if (hotbar.switchHint != null) {
				y += font.lineHeight + 2;
			}
		}
	}


	@Nullable
	private static AbilityBindUI makeBindUI(InputMethod inputMethod, KeyModifierMap binds, Power<?> abilityCtx, 
			@Nonnull KeyModifier modifier, AvailableAbilities available, ClientKeyWrapper key, boolean withModifierName, 
			AbilityIconSprites abilitySprites, @Nullable StandSkin standSkin, 
			Font font, TriState inContainerMenu) {
		List<PowerClassAbility> boundAbilities = binds.getAll(modifier);
		if (boundAbilities.isEmpty()) {
			return null;
		}
		AbilityConditionCheck ability = ClientControlScheme.prioritizedAbility(boundAbilities, available, abilityCtx, false);
		return makeAbilityBindUI(key, withModifierName ? modifier : null, 
				inputMethod, ability, abilityCtx, 
				abilitySprites, standSkin, 
				font, inContainerMenu);
	}

	@Nullable
	private static AbilityBindUI makeAbilityBindUI(ClientKeyWrapper key, @Nullable KeyModifier modifier, 
			InputMethod inputMethod, AbilityConditionCheck ability, Power<?> abilityCtx, 
			AbilityIconSprites abilitySprites, @Nullable StandSkin standSkin, 
			Font font, TriState inContainerMenu) {
		if (ability != null && ability.ability != null) {
			AbilityInputState state = AbilityInputState.withValue(ability.clientInputState);

			boolean showAbility = state.getFlag(AbilityInputState.IS_ACTIVE)
					|| state.getFlag(AbilityInputState.VISIBLE_EVEN_INACTIVE)
					|| state.getFlag(AbilityInputState.VISIBLE_TRANSLUCENT);
			showAbility &= state.getFlag(AbilityInputState.ONLY_IN_CONTAINER) == inContainerMenu.isTrue();

			if (showAbility) {
				Component keyName = getKeyName(key);
				if (modifier != null) {
					String modifierName = switch (modifier) {
					case CONTROL -> "ripples_hud.modifier_ctrl";
					case SHIFT -> "ripples_hud.modifier_shift";
					case ALT -> "ripples_hud.modifier_alt";
					default -> null;
					};
					if (modifierName != null) {
						keyName = Component.translatable("ripples_hud.key_modifier", Component.translatable(modifierName), keyName);
					}
				}
				Component bindName = inputMethod == InputMethod.HOLD ? Component.translatable("ripples_hud.hold_key", keyName) : keyName;

				AbilityBindUI bindUI = new AbilityBindUI();

				bindUI.ability = ability;
				bindUI.sprite = abilitySprites.getAbilityIcon(ability.ability, standSkin);
				bindUI.keybind = bindName;
				bindUI.keybindAbilityName = Component.translatable("ripples_hud.key_ability", bindName, ability.ability.getName(abilityCtx));

				return bindUI;
			}
		}
		return null;
	}


	public void renderControls(int x, int y, GuiGraphics guiGraphics, DeltaTracker deltaTracker, Font font, int textColor) {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		for (BindUI bind : this.binds) {
			GuiIcon hotbarSprite = getHotbarSprite(bind.abilities.size());
			if (hotbarSprite != null) {
				int x0 = x;
				int y0 = y;

				y += bind.y;
				int centered = (bind.keybindWidth - font.width(bind.keybind)) / 2;
				guiGraphics.drawString(font, bind.keybind, 
						x + centered, y + (SLOT_HEIGHT - font.lineHeight) / 2, 
						textColor);
				x += bind.keybindWidth;

				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				hotbarSprite.render(guiGraphics.pose(), x - 14, y - 14);

				for (Map.Entry<InputMethod, AbilityBindUI> abilitySprite : bind.abilities.entrySet()) {
					AbilityBindUI ability = abilitySprite.getValue();
					BlitFloat.blit(guiGraphics.pose(), Minecraft.getInstance(), ability.sprite, 
							x + 3, y + 3, 16, 16, 10, 
							abilityColor(BlitFloat.NO_TINT, ability.ability));
					x += SLOT_WIDTH;
				}

				x = x0;
				y = y0;
			}
		}

		for (HotbarUILine hotbar : this.hotbars) {
			int x0 = x;
			int y0 = y;

			GuiIcon hotbarSprite = getHotbarSprite(hotbar.slots.size());
			if (hotbarSprite != null) {
				y += hotbar.y;
				if (hotbar.keybind != null) {
					int centered = (hotbar.keybindWidth - font.width(hotbar.keybind)) / 2;
					guiGraphics.drawString(font, hotbar.keybind, 
							x + centered, y + (SLOT_HEIGHT - font.lineHeight) / 2, 
							textColor);
				}
				x += hotbar.keybindWidth;

				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				hotbarSprite.render(guiGraphics.pose(), x - 14, y - 14);

				for (HotbarSlotUI slot : hotbar.slots) {
					if (slot.sprite != null) {
						BlitFloat.blit(guiGraphics.pose(), Minecraft.getInstance(), slot.sprite.sprite, 
								x + 3, y + 3, 16, 16, 10, 
								abilityColor(BlitFloat.NO_TINT, slot.sprite.ability));
					}
					if (slot == hotbar.selected) {
						HOTBAR_SELECTION.render(guiGraphics.pose(), x - 15, y - 15);
					}
					x += SLOT_WIDTH;
				}

				x = x0;
				y += SLOT_HEIGHT + 4;
				if (hotbar.switchHint != null) {
					guiGraphics.drawString(font, hotbar.switchHint, x, y, textColor);
				}
				y += font.lineHeight + 4;
			}

			x = x0;
			y = y0;
		}

		RenderSystem.disableBlend();
	}

	@Nullable
	public GuiIcon getHotbarSprite(int elementCount) {
		return elementCount > 0 ? HOTBARS[Math.min(elementCount, HOTBARS.length) - 1] : null;
	}




	public static int abilityColor(int color, AbilityConditionCheck ability) {
		if (!ability.conditionCheck.isPositive()) {
			color = ARGB32.multiply(color, 0xFF606060);
		}
		AbilityInputState state = AbilityInputState.withValue(ability.clientInputState);
		if (state.getFlag(AbilityInputState.VISIBLE_TRANSLUCENT)) {
			color &= 0x40FFFFFF;
		}
		return color;
	}

	public static Component getKeyName(ClientKeyWrapper key) {
		if (key == ClientKeyWrapper.make(InputConstants.Type.MOUSE, InputConstants.MOUSE_BUTTON_LEFT)) {
			return Component.literal(Character.toString(IconSymbols.LMB_CLICK_LARGE));
		}
		else if (key == ClientKeyWrapper.make(InputConstants.Type.MOUSE, InputConstants.MOUSE_BUTTON_RIGHT)) {
			return Component.literal(Character.toString(IconSymbols.RMB_CLICK_LARGE));
		}
		else if (key == ClientKeyWrapper.make(InputConstants.Type.MOUSE, InputConstants.MOUSE_BUTTON_MIDDLE)) {
			return Component.literal(Character.toString(IconSymbols.MMB_CLICK_LARGE));
		}
		else {
			return ShortenText.shortenIfAble(key.keyName());
		}
	}



}
