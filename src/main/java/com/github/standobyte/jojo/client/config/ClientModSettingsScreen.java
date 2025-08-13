package com.github.standobyte.jojo.client.config;

import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.item.ItemIconModels;
import com.github.standobyte.jojo.client.ui.utils.Alignment;
import com.github.standobyte.jojo.client.ui.widgets.ButtonInLayout;
import com.github.standobyte.jojo.client.ui.widgets.ItemButton;
import com.github.standobyte.jojo.util.reflection.ClientReflection;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModList;

public class ClientModSettingsScreen extends Screen {
    protected final Screen lastScreen;
	protected final ClientModSettings settings;
	protected final ClientModSettings.Settings settingsValues;

	public ClientModSettingsScreen(Screen lastScreen, ClientModSettings settings) {
		this(lastScreen, settings, Component.translatable("jojo.options.client.title"));
	}

	public ClientModSettingsScreen(Screen lastScreen, ClientModSettings settings, Component title) {
		super(title);
		this.lastScreen = lastScreen;
		this.settings = settings;
		this.settingsValues = ClientModSettings.getSettingsReadOnly();
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.lastScreen);
	}

	@Override
	protected void init() {
		addRenderableWidgets();
	}

	protected void addRenderableWidgets() {
		int i = 0;

//		BooleanSetting characterVoiceLines = new BooleanSetting(settings, 
//				Component.translatable("jojo.config.client.characterVoiceLines"), 
//				Component.translatable("jojo.config.client.characterVoiceLines.tooltip")
//				) {
//			@Override public Boolean get() { return settingsValues.characterVoiceLines; }
//			@Override public void set(Boolean value) { settingsValues.characterVoiceLines = value; }
//		};
//		addRenderableWidget(characterVoiceLines.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));
//
//		BooleanSetting menacingParticles = new BooleanSetting(settings, 
//				Component.translatable("jojo.config.client.menacingParticles"), 
//				Component.translatable("jojo.config.client.menacingParticles.tooltip")
//				) {
//			@Override public Boolean get() { return settingsValues.menacingParticles; }
//			@Override public void set(Boolean value) { settingsValues.menacingParticles = value; }
//		};
//		addRenderableWidget(menacingParticles.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));

		i += (i % 2 == 1) ? 3 : 2;

		addRenderableWidget(new Button.Builder(
				Component.translatable("jojo.options.client.hud"), 
				button -> minecraft.setScreen(new HudSettings(this, settings, button.getMessage())))
				.bounds(calcButtonX(i), calcButtonY(i++) + 6, 150, 20).build(/*Button::new*/));

		addRenderableWidget(new Button.Builder(
				Component.translatable("jojo.options.client.stand"), 
				button -> minecraft.setScreen(new StandSettings(this, settings, button.getMessage())))
				.bounds(calcButtonX(i), calcButtonY(i++) + 6, 150, 20).build(/*Button::new*/));

		addRenderableWidget(new Button.Builder(
				Component.translatable("jojo.options.client.hamon"), 
				button -> minecraft.setScreen(new HamonSettings(this, settings, button.getMessage())))
				.bounds(calcButtonX(i), calcButtonY(i++) + 6, 150, 20).build(/*Button::new*/));

		addRenderableWidget(new Button.Builder(
				Component.translatable("jojo.options.client.vampirism"), 
				button -> minecraft.setScreen(new VampirismSettings(this, settings, button.getMessage())))
				.bounds(calcButtonX(i), calcButtonY(i++) + 6, 150, 20).build(/*Button::new*/));

		addBackButton(CommonComponents.GUI_DONE, i);
	}

	protected void addBackButton(Component text, int buttonsAdded) {
		buttonsAdded += 2;
		if (buttonsAdded % 2 == 1) {
			++buttonsAdded;
		}

		addRenderableWidget(new Button.Builder(
				text, button -> minecraft.setScreen(lastScreen))
				.bounds(this.width / 2 - 100, 
						calcButtonY(buttonsAdded), 
						200, 20)
				.build(/*Button::new*/));
	}



	public static class HudSettings extends ClientModSettingsScreen {

		public HudSettings(Screen lastScreen, ClientModSettings settings, Component title) {
			super(lastScreen, settings, title);
		}

		@Override
		protected void addRenderableWidgets() {
			int i = 0;

//			EnumSetting<PositionConfig> barsPosition = new EnumSetting<PositionConfig>(settings, 
//					Component.translatable("jojo.config.client.barsPosition"), 
//					Component.translatable("jojo.config.client.barsPosition.tooltip"), 
//					PositionConfig.class) {
//				@Override public PositionConfig get() { return settingsValues.barsPosition; }
//				@Override public void set(PositionConfig value) { settingsValues.barsPosition = value; }
//			};
//			addRenderableWidget(barsPosition.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));
//
//
//			EnumSetting<PositionConfig> hotbarsPosition = new EnumSetting<PositionConfig>(settings, 
//					Component.translatable("jojo.config.client.hotbarsPosition"), 
//					Component.translatable("jojo.config.client.hotbarsPosition.tooltip"), 
//					PositionConfig.class) {
//				@Override public PositionConfig get() { return settingsValues.hotbarsPosition; }
//				@Override public void set(PositionConfig value) { settingsValues.hotbarsPosition = value; }
//			};
//			addRenderableWidget(hotbarsPosition.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));
//
//
//			EnumSetting<HudTextRender> hudNamesRender = new EnumSetting<HudTextRender>(settings, 
//					Component.translatable("jojo.config.client.hudNamesRender"), 
//					Component.translatable("jojo.config.client.hudNamesRender.tooltip"), 
//					HudTextRender.class) {
//				@Override public HudTextRender get() { return settingsValues.hudTextRender; }
//				@Override public void set(HudTextRender value) { settingsValues.hudTextRender = value; }
//			};
//			addRenderableWidget(hudNamesRender.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));
//
//
//			BooleanSetting hudHotbarsFold = new BooleanSetting(settings, 
//					Component.translatable("jojo.config.client.hudHotbarsFold"), 
//					Component.translatable("jojo.config.client.hudHotbarsFold.tooltip")
//					) {
//				@Override public Boolean get() { return settingsValues.hudHotbarFold; }
//				@Override public void set(Boolean value) { 
//					settingsValues.hudHotbarFold = value;
//					if (minecraft.player != null) {
//						for (PowerClass<?> power : PowerClass.values()) {
//							power.getOptional(minecraft.player).ifPresent(Power::clUpdateHud);
//						}
//					}
//				}
//			};
//			addRenderableWidget(hudHotbarsFold.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));
//
//
//			BooleanSetting showLockedSlots = new BooleanSetting(settings, 
//					Component.translatable("jojo.config.client.showLockedSlots"), 
//					Component.translatable("jojo.config.client.showLockedSlots.tooltip")
//					) {
//				@Override public Boolean get() { return settingsValues.showLockedSlots; }
//				@Override public void set(Boolean value) {
//					settingsValues.showLockedSlots = value;
//					if (minecraft.player != null) {
//						for (PowerClass<?> power : PowerClass.values()) {
//							power.getOptional(minecraft.player).ifPresent(Power::clUpdateHud);
//						}
//					}
//				}
//			};
//			addRenderableWidget(showLockedSlots.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));

			addBackButton(CommonComponents.GUI_BACK, i);
		}

	}

	public static class StandSettings extends ClientModSettingsScreen {

		public StandSettings(Screen lastScreen, ClientModSettings settings, Component title) {
			super(lastScreen, settings, title);
		}

		@Override
		protected void addRenderableWidgets() {
			int i = 0;

//			BooleanSetting resolveShaders = new BooleanSetting(settings, 
//					Component.translatable("jojo.config.client.resolveShaders"), 
//					Component.translatable("jojo.config.client.resolveShaders.tooltip")
//					) {
//				@Override public Boolean get() { return settingsValues.resolveShaders; }
//				@Override public void set(Boolean value) { 
//					settingsValues.resolveShaders = value;
//					if (!value) {
//						ShaderEffectApplier.getInstance().stopResolveShader();
//					}
//				}
//			};
//			addRenderableWidget(resolveShaders.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));
//
//
//			BooleanSetting timeStopAnimation = new BooleanSetting(settings, 
//					Component.translatable("jojo.config.client.timeStopAnimation"), 
//					Component.translatable("jojo.config.client.timeStopAnimation.tooltip")
//					) {
//				@Override public Boolean get() { return settingsValues.timeStopAnimation; }
//				@Override public void set(Boolean value) { settingsValues.timeStopAnimation = value; }
//			};
//			addRenderableWidget(timeStopAnimation.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));
//
//
//			Setting<HumanoidArm> standSide = new EnumSetting<HumanoidArm>(settings, 
//					Component.translatable("jojo.config.client.standSide"), 
//					Component.translatable("jojo.config.client.standSide.tooltip"), 
//					HumanoidArm.class) {
//				@Override public HumanoidArm get() { return settingsValues.broadcasted.standSide; }
//				@Override public void set(HumanoidArm value) { settingsValues.broadcasted.standSide = value; }
//			}
//			.prefix("stand_")
//			.setBroadcasted();
//			addRenderableWidget(standSide.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));
//
//
//			BooleanSetting standMotionTilt = new BooleanSetting(settings, 
//					Component.translatable("jojo.config.client.standMotionTilt"), 
//					Component.translatable("jojo.config.client.standMotionTilt.tooltip")
//					) {
//				@Override public Boolean get() { return settingsValues.standMotionTilt; }
//				@Override public void set(Boolean value) { settingsValues.standMotionTilt = value; }
//			};
//			addRenderableWidget(standMotionTilt.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));
//
//			BooleanSetting standOutline = new BooleanSetting(settings, 
//					Component.translatable("jojo.config.client.standOutline"), 
//					Component.translatable("jojo.config.client.standOutline.tooltip")
//					) {
//				@Override public Boolean get() { return settingsValues.standOutline; }
//				@Override public void set(Boolean value) { settingsValues.standOutline = value; }
//			};
//			addRenderableWidget(standOutline.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));

			addBackButton(CommonComponents.GUI_BACK, i);
		}

	}

	public static class HamonSettings extends ClientModSettingsScreen {

		public HamonSettings(Screen lastScreen, ClientModSettings settings, Component title) {
			super(lastScreen, settings, title);
		}

		@Override
		protected void addRenderableWidgets() {
			int i = 0;

			BooleanSetting thirdPersonHamonAura = new BooleanSetting(settings, 
					Component.translatable("jojo.config.client.thirdPersonHamonAura"), 
					Component.translatable("jojo.config.client.thirdPersonHamonAura.tooltip")
					) {
				@Override public Boolean get() { return settingsValues.thirdPersonHamonAura; }
				@Override public void set(Boolean value) { 
					settingsValues.thirdPersonHamonAura = value;
				}
			};
			addRenderableWidget(thirdPersonHamonAura.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));

			BooleanSetting firstPersonHamonAura = new BooleanSetting(settings, 
					Component.translatable("jojo.config.client.firstPersonHamonAura"), 
					Component.translatable("jojo.config.client.firstPersonHamonAura.tooltip")
					) {
				@Override public Boolean get() { return settingsValues.firstPersonHamonAura; }
				@Override public void set(Boolean value) { 
					settingsValues.firstPersonHamonAura = value;
				}
			};
			addRenderableWidget(firstPersonHamonAura.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));

			BooleanSetting hamonAuraBlur = new BooleanSetting(settings, 
					Component.translatable("jojo.config.client.hamonAuraBlur"), 
					Component.translatable("jojo.config.client.hamonAuraBlur.tooltip")
					) {
				@Override public Boolean get() { return settingsValues.hamonAuraBlur; }
				@Override public void set(Boolean value) { 
					settingsValues.hamonAuraBlur = value;
				}
			};
			addRenderableWidget(hamonAuraBlur.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));

			addBackButton(CommonComponents.GUI_BACK, i);
		}

	}

	public static class VampirismSettings extends ClientModSettingsScreen {

		public VampirismSettings(Screen lastScreen, ClientModSettings settings, Component title) {
			super(lastScreen, settings, title);
		}

		@Override
		protected void addRenderableWidgets() {
			int i = 0;

//			BooleanSetting glowingEyes = new BooleanSetting(settings, 
//					Component.translatable("jojo.config.client.vampireGlowingEyes"), 
//					Component.translatable("jojo.config.client.vampireGlowingEyes.tooltip")
//					) {
//				@Override public Boolean get() { return settingsValues.broadcasted.vampireGlowingEyes; }
//				@Override public void set(Boolean value) { 
//					settingsValues.broadcasted.vampireGlowingEyes = value;
//				}
//			};
//			addRenderableWidget(glowingEyes.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));

			addBackButton(CommonComponents.GUI_BACK, i);
		}

	}



	protected int calcButtonX(int i) {
		return this.width / 2 - 155 + i % 2 * 160;
	}

	protected int calcButtonY(int i) {
		return this.height / 6 - 12 + 24 * (i >> 1);
	}

	@Override
	public void removed() {
		settings.save();
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
		guiGraphics.drawCenteredString(font, title, width / 2, 15, 0xFFFFFF);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
	}



	protected static abstract class Setting<T> {
		protected boolean broadcast = false;

		public abstract T get();
		public abstract void set(T value);

		public Setting<T> setBroadcasted() {
			this.broadcast = true;
			return this;
		}

		public abstract Button createButton(int x, int y, int width, int height, Screen screen, int buttonI);
	}

	protected static abstract class BooleanSetting extends Setting<Boolean> {
		private final ClientModSettings settings;
		private final Component name;
		private final Component tooltip;

		public BooleanSetting(ClientModSettings settings, Component name, @Nullable Component tooltip) {
			this.settings = settings;
			this.name = name;
			this.tooltip = tooltip;
		}

		@Override
		public Button createButton(int x, int y, int width, int height, Screen screen, int buttonI) {
			return new ScrollingStringButton(
					x, y, width, height,
					CommonComponents.optionStatus(name, get()), 
					button -> {
						settings.editSettings(s -> {
							set(!get());
							button.setMessage(CommonComponents.optionStatus(name, get()));
						}, broadcast);
					},
					Tooltip.create(tooltip))
					.setAlignment(buttonI % 2 == 0 ? Alignment.LEFT : Alignment.RIGHT);
		}
	}

	protected static abstract class EnumSetting<T extends Enum<T>> extends Setting<T> {
		private final ClientModSettings settings;
		private final Component name;
		private final Component tooltip;
		private final Class<T> enumClass;
		private String prefix = "jojo.config.client.option.";

		public EnumSetting(ClientModSettings settings, Component name, @Nullable Component tooltip, Class<T> enumClass) {
			this.settings = settings;
			this.name = name;
			this.enumClass = enumClass;
			this.tooltip = tooltip;
		}

		public EnumSetting<T> prefix(String prefix) {
			this.prefix += prefix;
			return this;
		}

		@Override
		public Button createButton(int x, int y, int width, int height, Screen screen, int buttonI) {
			return new ScrollingStringButton(
					x, y, width, height,
					Component.translatable("options.generic_value", name, getValueMessage(get())), 
					button -> {
						settings.editSettings(s -> {
							T[] values = enumClass.getEnumConstants();
							T val = get();
							T nextVal = values[(val.ordinal() + 1) % values.length];
							set(nextVal);
							button.setMessage(Component.translatable("options.generic_value", name, getValueMessage(nextVal)));
						}, broadcast);
					},
					Tooltip.create(tooltip))
					.setAlignment(buttonI % 2 == 0 ? Alignment.LEFT : Alignment.RIGHT);
		}

		private Component getValueMessage(T value) {
			return Component.translatable(prefix + value.name().toLowerCase());
		}
    }
    
    
    
	// yes, the modern versions have scrolling text too, but this one renders full text when the button is hovered
    private static class ScrollingStringButton extends Button {
        private Alignment alignment = Alignment.LEFT;
        
        public ScrollingStringButton(int pX, int pY, int pWidth, int pHeight, Component pMessage,
        		Button.OnPress pOnPress) {
            super(new Button.Builder(pMessage, pOnPress).bounds(pX, pY, pWidth, pHeight));
        }
        
        public ScrollingStringButton(int pX, int pY, int pWidth, int pHeight, Component pMessage, 
        		Button.OnPress pOnPress, Tooltip pOnTooltip) {
            super(new Button.Builder(pMessage, pOnPress).bounds(pX, pY, pWidth, pHeight).tooltip(pOnTooltip));
        }
        
        public ScrollingStringButton setAlignment(Alignment alignment) {
            this.alignment = alignment;
            return this;
        }

        @Override
        protected void renderScrollingString(GuiGraphics guiGraphics, Font font, int width, int color) {
        	int x0 = this.getX() + width;
        	int x1 = this.getX() + this.getWidth() - width;
        	int y0 = this.getY();
        	int y1 = this.getY() + this.getHeight();

        	Component text = getMessage();
            int textWidth = font.width(text);
            int y = (y0 + y1 - 9) / 2 + 1;
            int buttonWidth = x1 - x0;
            if (textWidth > buttonWidth && isHovered()) {
                switch (alignment) {
                case LEFT:
                    guiGraphics.drawString(font, text, x0, y, color);
                    break;
                case RIGHT:
                    guiGraphics.drawString(font, text, x1 - textWidth, y, color);
                    break;
                }
            }
            else {
            	renderScrollingString(guiGraphics, font, text, x0, y0, x1, y1, color);
            }
        }
    }
    
    
    
    
    public static void addSettingsButton(OptionsScreen optionsScreen, 
    		List<AbstractWidget> otherModdedButtons, Consumer<GuiEventListener> addButton) {
		Component tooltip = Component.translatable("jojo.options.client.title");
		ItemButton settingsButton = new ItemButton(-1, -1, 20, 20, 
				ItemIconModels.makeIconItem(ItemIconModels.MOD_LOGO),
				__ -> optionsScreen.getMinecraft().setScreen(new ClientModSettingsScreen(optionsScreen, ClientModSettings.getInstance())),
				Tooltip.create(tooltip),
				tooltip);
		Layout fuckTheseAbstractions = new ButtonInLayout(settingsButton, button -> {
			int[] buttonPos = findPosForButton(optionsScreen, otherModdedButtons, button);
			button.setX(buttonPos[0]);
			button.setY(buttonPos[1]);
		});
		ClientReflection.getLayout(optionsScreen).addToContents(fuckTheseAbstractions);
		fuckTheseAbstractions.arrangeElements();
		addButton.accept(settingsButton);
	}
    
    private static int[] findPosForButton(Screen optionsScreen, List<AbstractWidget> otherModdedButtons, AbstractWidget except) {
		final int minY = 87;
		final int maxY = minY + 100;

		final int minX1 = 0;
		final int maxX1 = optionsScreen.width / 2 - 155 - 20 - 5;
		final int minX2 = optionsScreen.width / 2 + 160;
		final int maxX2 = optionsScreen.width - 20;

		final int minX3 = optionsScreen.width / 2 - 155;
		final int maxX3 = minX3 + 290;
		final int y3 = maxY + 24;

		int[] buttonPos = null;

		// try placing the button to the right side
		for (int x = minX2; x <= maxX2 && buttonPos == null; x += 25) {
			int y = maxY;
			if (ModList.get().isLoaded("essential")) y -= 24; // for fuck's sake
			for (; y >= minY && buttonPos == null; y -= 24) {
				buttonPos = noOverlapPos(otherModdedButtons, except, x, y);
			}
		}
		// ...or to the left side
		if (buttonPos == null) {
			for (int x = maxX1; x >= minX1 && buttonPos == null; x -= 25) {
				for (int y = maxY; y >= minY && buttonPos == null; y -= 24) {
					buttonPos = noOverlapPos(otherModdedButtons, except, x, y);
				}
			}
		}
		// ...or below the vanilla options
		if (buttonPos == null) {
			for (int x = minX3; x <= maxX3 && buttonPos == null; x += 29) {
				buttonPos = noOverlapPos(otherModdedButtons, except, x, y3);
			}
		}
		// ...how many new buttons are there?? fuck it, just put it at the "Done" button
		if (buttonPos == null) {
			buttonPos = new int[] { optionsScreen.width / 2 + 110, optionsScreen.height - 26 };
		}
		
		return buttonPos;
    }

	@Nullable
	private static int[] noOverlapPos(List<AbstractWidget> buttonsList, AbstractWidget except, int x, int y) {
		int x2 = x + 20;
		int y2 = y + 20;
		return buttonsList.stream().filter(button -> button != except).anyMatch(button -> {
			return button.getX() < x2 && button.getX() + button.getWidth() > x && button.getY() < y2 && button.getY() + button.getHeight() > y;
		}) ? null : new int[] { x, y };
	}

}
