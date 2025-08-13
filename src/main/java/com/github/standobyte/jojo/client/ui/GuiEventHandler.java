package com.github.standobyte.jojo.client.ui;

import java.util.List;

import com.github.standobyte.jojo.client.config.ClientModSettingsScreen;
import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class GuiEventHandler {

//	@SubscribeEvent
//	public static void afterScreenRender(DrawScreenEvent.Post event) {
//		Screen screen = event.getGui();
//		float partialTick = screen.getMinecraft().getFrameTime();
//		if (screen instanceof DeathScreen) {
//			Component title = screen.getTitle();
//			if (title instanceof TranslationTextComponent && ((TranslationTextComponent) title).getKey().endsWith(".hardcore")) {
//				return;
//			}
//			renderToBeContinuedArrow(event.getMatrixStack(), screen, screen.width, screen.height, partialTick);
//		}
//
//		else if (screen instanceof IngameMenuScreen && ClientReflection.showsPauseMenu((IngameMenuScreen) screen)) {
//			float alpha = ClientModSettings.getSettingsReadOnly().standStatsTranslucency;
//			boolean invertBnW = ClientModSettings.getSettingsReadOnly().standStatsInvertBnW;
//			int xButtonsRightEdge = screen.width / 2 + 102;
//			int windowWidth = screen.width;
//			int windowHeight = screen.height;
//
//			if (doStandStatsRender(screen)) {
//				StandStatsRenderer.renderStandStats(event.getMatrixStack(), mc, 
//						windowWidth - StandStatsRenderer.statsWidth - 7, windowHeight - StandStatsRenderer.statsHeight - 7, 
//						windowWidth, windowHeight,
//						standStatsTick, partialTick, 
//						alpha, invertBnW,
//						event.getMouseX(), event.getMouseY(), windowWidth - xButtonsRightEdge - 14);
//			}
//		}
//	}
//
//	private static Boolean renderStandStats;
//	private static boolean doStandStatsRender(Screen screen) {
//		if (renderStandStats != null) {
//			return renderStandStats;
//		}
//		int xButtonsRightEdge = screen.width / 2 + 102;
//		int windowWidth = screen.width;
//		int windowHeight = screen.height;
//		return windowWidth - xButtonsRightEdge >= 167 && windowHeight > 204;
//	}
//
//	private static void renderToBeContinuedArrow(MatrixStack matrixStack, AbstractGui ui, int screenWidth, int screenHeight, float partialTick) {
//		int x = screenWidth - 5 - (int) ((screenWidth - 10) * Math.min(deathScreenTick + partialTick, 20F) / 20F);
//		int y = screenHeight - 29;
//		mc.textureManager.bind(ClientUtil.ADDITIONAL_UI);
//		ui.blit(matrixStack, x, y, 0, 231, 130, 25);
//		AbstractGui.drawCenteredString(matrixStack, mc.font, Component.translatable("jojo.to_be_continued"), x + 61, y + 8, 0x525544);
//	}
//
//	@SubscribeEvent
//	public static void onTooltipRender(RenderTooltipEvent.PostText event) {
//		List<? extends ITextProperties> lines = event.getLines();
//		int x = event.getX();
//		int y = event.getY();
//		for (int i = 0; i < lines.size(); i++) {
//			ITextProperties line = lines.get(i);
//			if (line instanceof JojoTextComponentWrapper) {
//				((JojoTextComponentWrapper) line).tooltipRenderExtra(event.getMatrixStack(), x, y - 0.5f);
//			}
//			if (i == 0) {
//				y += 2;
//			}
//			y += 10;
//		}
//	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void addToScreen(ScreenEvent.Init.Post event) {
		Screen screen = event.getScreen();
//		if (screen instanceof IngameMenuScreen && ClientReflection.showsPauseMenu((IngameMenuScreen) screen)) {
//			IStandPower.getStandPowerOptional(mc.player).ifPresent(power -> {
//				if (power.hasPower()) {
//					AbstractSlider statsBgAlphaSlider = new HeightScaledSlider(
//							screen.width - 160, screen.height - 6, 153, 6, StringTextComponent.EMPTY, 0.0D) {
//						{
//							this.value = MathHelper.inverseLerp(
//									ClientModSettings.getSettingsReadOnly().standStatsTranslucency, 
//									0.1, 1.0);
//							updateMessage();
//						}
//
//						@Override
//						protected void updateMessage() {
//							setMessage(StringTextComponent.EMPTY);
//						}
//
//						@Override
//						protected void applyValue() {
//							ClientModSettings.getInstance().editSettings(settings -> {
//								settings.standStatsTranslucency = (float) MathHelper.clampedLerp(0.1, 1.0, this.value);
//							});
//						}
//					};
//					statsBgAlphaSlider.visible = doStandStatsRender(screen);
//					event.addWidget(statsBgAlphaSlider);
//
//					ImageMutableButton invertBnWButton = new ImageMutableButton(screen.width - 8, screen.height - 7, 
//							8, 8, 464, 496, 8, StandStatsRenderer.STAND_STATS_UI, 512, 512, 
//							button -> {
//								ClientModSettings.getInstance().editSettings(settings -> {
//									settings.standStatsInvertBnW = !settings.standStatsInvertBnW;
//									((ImageMutableButton) button).xTexStart = settings.standStatsInvertBnW ? 472 : 464;
//								});
//							});
//					invertBnWButton.xTexStart = ClientModSettings.getSettingsReadOnly().standStatsInvertBnW ? 472 : 464;
//					invertBnWButton.visible = doStandStatsRender(screen);
//					event.addWidget(invertBnWButton);
//
//					Button standStatsToggleButton = new ImageVanillaButton(screen.width - 28, screen.height - 28, 
//							20, 20, 492, 492, StandStatsRenderer.STAND_STATS_UI, 512, 512, 
//							button -> {
//								renderStandStats = !doStandStatsRender(screen);
//								statsBgAlphaSlider.visible = doStandStatsRender(screen);
//								invertBnWButton.visible = doStandStatsRender(screen);
//							}, 
//							(button, matrixStack, x, y) -> {
//								Component message = doStandStatsRender(screen) ? 
//										Component.translatable("jojo.stand_stat.button.hide")
//										: Component.translatable("jojo.stand_stat.button.show");
//								screen.renderTooltip(matrixStack, message, x, y);
//							}, 
//							StringTextComponent.EMPTY);
//					event.addWidget(standStatsToggleButton);
//				}
//			});
//		}

		/*else*/ if (screen instanceof OptionsScreen options) {
			List<AbstractWidget> buttons = event.getListenersList().stream()
					.filter(b -> b instanceof AbstractWidget)
					.map(b -> (AbstractWidget) b)
					.toList();
			ClientModSettingsScreen.addSettingsButton(options, buttons, event::addListener);
		}

//		else if (screen instanceof ControlsScreen) {
//			KeyBindingList controlList = ClientReflection.getControlList((ControlsScreen) screen);
//			List<KeyBindingList.Entry> keyEntries = controlList.children();
//
//			ListIterator<KeyBindingList.Entry> entriesIter = keyEntries.listIterator();
//			ClientModSettings modSettings = ClientModSettings.getInstance();
//			ClientModSettings.Settings modSettingsRead = ClientModSettings.getSettingsReadOnly();
//
//			boolean addHudScreenButtons;
//			LazyOptional<IStandPower> spOptional;
//			LazyOptional<INonStandPower> nspOptional;
//			if (mc.player != null) {
//				spOptional = IStandPower.getStandPowerOptional(mc.player);
//				nspOptional = INonStandPower.getNonStandPowerOptional(mc.player);
//				addHudScreenButtons = spOptional.map(IPower::hasPower).orElse(false) || nspOptional.map(IPower::hasPower).orElse(false);
//			}
//			else {
//				addHudScreenButtons = false;
//				spOptional = LazyOptional.empty();
//				nspOptional = LazyOptional.empty();
//			}
//
//			while (entriesIter.hasNext()) {
//				KeyBindingList.Entry entry = entriesIter.next();
//				if (entry instanceof KeyBindingList.KeyEntry) {
//					KeyBindingList.KeyEntry keyEntry = (KeyBindingList.KeyEntry) entry;
//					KeyBinding key = ClientReflection.getKey(keyEntry);
//					if (key == InputHandler.getInstance().attackHotbar) {
//						entriesIter.set(new HoldToggleKeyEntry(keyEntry, ClientReflection.getChangeButton(keyEntry), new ControlSettingToggleButton(40, 20, 
//								button -> {
//									modSettings.editSettings(s -> s.toggleLmbHotbar = !s.toggleLmbHotbar);
//									InputHandler.getInstance().setToggledHotbarControls(ControlScheme.Hotbar.LEFT_CLICK, false);
//								},
//								() -> modSettingsRead.toggleLmbHotbar)));
//					}
//					else if (key == InputHandler.getInstance().abilityHotbar) {
//						entriesIter.set(new HoldToggleKeyEntry(keyEntry, ClientReflection.getChangeButton(keyEntry), new ControlSettingToggleButton(40, 20, 
//								button -> {
//									modSettings.editSettings(s -> s.toggleRmbHotbar = !s.toggleRmbHotbar);
//									InputHandler.getInstance().setToggledHotbarControls(ControlScheme.Hotbar.RIGHT_CLICK, false);
//								},
//								() -> modSettingsRead.toggleRmbHotbar)));
//					}
//					else if (key == InputHandler.getInstance().disableHotbars) {
//						entriesIter.set(new HoldToggleKeyEntry(keyEntry, ClientReflection.getChangeButton(keyEntry), new ControlSettingToggleButton(40, 20, 
//								button -> {
//									modSettings.editSettings(s -> s.toggleDisableHotbars = !s.toggleDisableHotbars);
//									InputHandler.getInstance().setToggleHotbarsDisabled(false);
//								},
//								() -> modSettingsRead.toggleDisableHotbars)));
//					}
//				}
//				else if (addHudScreenButtons && entry instanceof KeyBindingList.CategoryEntry) {
//					KeyBindingList.CategoryEntry categoryEntry = (KeyBindingList.CategoryEntry) entry;
//					Component categoryName = ClientReflection.getName(categoryEntry);
//
//					IStandPower standPower = spOptional.resolve().get();
//					INonStandPower nonStandPower = nspOptional.resolve().get();
//					Button[] hudScreenButtons = new Button[standPower.hasPower() && nonStandPower.hasPower() ? 2 : 1];
//					int i = 0;
//					if (standPower.hasPower()) {
//						Component tooltip = Component.translatable("jojo.key.edit_hud.power_name", standPower.getName());
//						hudScreenButtons[i++] = new ImageVanillaButton((screen.width + mc.font.width(categoryName) + 10) / 2, -21, 
//								20, 20, 
//								0, 0, 16, 16, standPower.clGetPowerTypeIcon(), 16, 16, 
//								button -> {
//									HudLayoutEditingScreen hudScreen = new HudLayoutEditingScreen(PowerClassification.STAND);
//									mc.setScreen(hudScreen);
//								}, 
//								(button, matrixStack, mouseX, mouseY) -> screen.renderTooltip(matrixStack, tooltip, mouseX, mouseY),
//								tooltip);
//					}
//					if (nonStandPower.hasPower()) {
//						Component tooltip = Component.translatable("jojo.key.edit_hud.power_name", nonStandPower.getName());
//						hudScreenButtons[i] = new ImageVanillaButton((screen.width + mc.font.width(categoryName) + 10) / 2 + (i++) * 24, -21, 
//								20, 20, 
//								0, 0, 16, 16, nonStandPower.clGetPowerTypeIcon(), 16, 16, 
//								button -> {
//									HudLayoutEditingScreen hudScreen = new HudLayoutEditingScreen(PowerClassification.NON_STAND);
//									mc.setScreen(hudScreen);
//								}, 
//								(button, matrixStack, mouseX, mouseY) -> screen.renderTooltip(matrixStack, tooltip, mouseX, mouseY),
//								tooltip);
//					}
//
//					if (InputHandler.HUD_CATEGORY.equals(((TranslationTextComponent) categoryName).getKey())) {
//						entriesIter.set(new CategoryWithButtonsEntry(controlList, categoryName, hudScreenButtons));
//					}
//				}
//			}
//
//			if (HudLayoutEditingScreen.scrollCtrlListTo != null) {
//				Predicate<KeyBindingList.Entry> scrollTo = HudLayoutEditingScreen.scrollCtrlListTo;
//				HudLayoutEditingScreen.scrollCtrlListTo = null;
//				OptionalInt index = IntStream.range(0, controlList.children().size())
//						.filter(i -> {
//							KeyBindingList.Entry entry = controlList.children().get(i);
//							return scrollTo.test(entry);
//						})
//						.findFirst();
//				index.ifPresent(i -> {
//					controlList.setScrollAmount(ClientReflection.getRowTop(controlList, i) - controlList.getTop());
//				});
//			}
//		}
//
//		else if (screen instanceof ChatScreen) {
//			Entity possessed = IPlayerPossess.getPossessedEntity(mc.player);
//			if (possessed != null && possessed.getType() == ModEntityTypes.ANGELO_ROCK.get()) {
//				int x = screen.width / 2 - 100;
//				int y = screen.height - 40;
//				Button angeloRockDieButton = new Button(x, y, 200, 20, 
//						Component.translatable(mc.level.getLevelData().isHardcore() ? "deathScreen.spectate" : "deathScreen.respawn"), 
//						button -> PacketManager.sendToServer(ClAngeloRockButtonPacket.respawn()));
//				event.addWidget(angeloRockDieButton);
//
//				Button angeloRockGruntButton = new ImageVanillaButton(x - 24, y, 20, 20, 
//						238, 150, 
//						ClientUtil.ADDITIONAL_UI, 256, 256,
//						button -> PacketManager.sendToServer(ClAngeloRockButtonPacket.grunt())) {
//					@Override public void playDownSound(SoundHandler pHandler) {}
//				};
//				event.addWidget(angeloRockGruntButton);
//			}
//		}
	}

//	@SubscribeEvent
//	public static void onScreenOpened(GuiOpenEvent event) {
//		Screen screen = event.getGui();
//		if (screen instanceof MainMenuScreen) {
//			String splash = CustomResources.getModSplashes().overrideSplash();
//			if (splash != null) {
//				ClientReflection.setSplash((MainMenuScreen) screen, splash);
//			}
//		}
//		else if (screen instanceof IngameMenuScreen) {
//			IStandPower.getStandPowerOptional(mc.player).resolve()
//			.map(StandStatsRenderer.ICosmeticStandStats::getHandler)
//			.ifPresent(StandStatsRenderer.ICosmeticStandStats::onPauseScreenOpened);
//		}
//		else if (screen == null) {
//			onScreenClosed();
//		}
//	}
//
//	@SubscribeEvent(priority = EventPriority.LOWEST)
//	public static void onScreenOpened2(GuiOpenEvent event) {
//		IJojoScreen.rememberScreenTab(event.getGui());
//	}
//
//	private static void onScreenClosed() {
//		if (renderStandStats != null && renderStandStats) {
//			renderStandStats = null;
//		}
//	}
}
