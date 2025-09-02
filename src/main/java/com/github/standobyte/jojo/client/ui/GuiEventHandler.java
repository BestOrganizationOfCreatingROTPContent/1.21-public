package com.github.standobyte.jojo.client.ui;

//@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
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
//	@SubscribeEvent(priority = EventPriority.LOW)
//	public static void addToScreen(ScreenEvent.Init.Post event) {
//		Screen screen = event.getScreen();
//		if (screen instanceof ControlsScreen) {
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
//	}
//
//	@SubscribeEvent
//	public static void onScreenOpened(GuiOpenEvent event) {
//		Screen screen = event.getGui();
//		if (screen instanceof MainMenuScreen) {
//			String splash = CustomResources.getModSplashes().overrideSplash();
//			if (splash != null) {
//				ClientReflection.setSplash((MainMenuScreen) screen, splash);
//			}
//		}
//	}
//
//	@SubscribeEvent(priority = EventPriority.LOWEST)
//	public static void onScreenOpened2(GuiOpenEvent event) {
//		IJojoScreen.rememberScreenTab(event.getGui());
//	}
}
