package com.github.standobyte.jojo.client.ui.powerhud;

import java.util.HashMap;
import java.util.Map;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.ClientPowerCache;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.v1_21_4_stuff.missingmethods.ARGB;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.TriState;

@EventBusSubscriber(value = Dist.CLIENT, bus = Bus.MOD)
public class PowerHud {
	public static PrototypeAbilityHud abilityHUDInstance;

	@SubscribeEvent
	public static void addHud(RegisterGuiLayersEvent event) {
		event.registerAboveAll(JojoMod.resLoc("ability_hud"), abilityHUDInstance = new PrototypeAbilityHud());
		NeoForge.EVENT_BUS.register(new GameBusEventHandler());
	}
	
	
	public static boolean hasElementTooltips(Screen screen) {
		return screen instanceof ChatScreen
				|| /*F3+Esc*/ screen instanceof PauseScreen pause && !pause.showsPauseMenu();
	}
	
	public static boolean canDragElementsOn(Screen screen) {
		return screen instanceof ChatScreen
				|| screen instanceof PauseScreen pause && !pause.showsPauseMenu();
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
			abilityHUDInstance.setupRender(TriState.TRUE);
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
		
		
		public TriState inContainerMenu;
		private int mouseX;
		private int mouseY;
		
		public void setupRender(TriState inContainerMenu) {
			setupRender(inContainerMenu, -1, -1);
		}
		
		public void setupRender(TriState inContainerMenu, int mouseX, int mouseY) {
			this.inContainerMenu = inContainerMenu;
			this.mouseX = mouseX;
			this.mouseY = mouseY;
		}

	
		public HudElement controls = addElement(new ControlsHudElement("controls", 4, 44, -1, -1));
		public HudElement resolveBar = addElement(new Resolve("resolve_bar", 11, 12, 32, 16));
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
					setupRender(TriState.FALSE, mouseX, mouseY);
				}
				else {
					setupRender(TriState.FALSE);
				}
				renderAbilitiesHUD(guiGraphics, deltaTracker);
			}
			setupRender(TriState.DEFAULT);
			renderAbilitiesHUD(guiGraphics, deltaTracker);
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
		
		
	public static class Resolve extends HudElement {
		public static final GuiIcon RESOLVE_MODE = new GuiIcon(JojoMod.resLoc("textures/hud/stand_resolve_mode_bar.png"), 40, 40);
		public static final GuiIcon HORIZONTAL_EMPTY = new GuiIcon(JojoMod.resLoc("textures/hud/stand_resolve_horizontal_empty.png"), 32, 16);
		public static final GuiIcon HORIZONTAL_FULL = new GuiIcon(JojoMod.resLoc("textures/hud/stand_resolve_horizontal_full.png"), 32, 16);
		public static final GuiIcon VERTICAL_EMPTY = new GuiIcon(JojoMod.resLoc("textures/hud/stand_resolve_vertical_empty.png"), 16, 32);
		public static final GuiIcon VERTICAL_FULL = new GuiIcon(JojoMod.resLoc("textures/hud/stand_resolve_vertical_full.png"), 16, 32);

		public Resolve(String name, int x0, int y0, int width, int height) {
			super(name, x0, y0, width, height);
		}

		public Resolve(String name, SnappingH snappingHorizontal, SnappingV snappingVertical, 
				int xOffset, int yOffset, int width, int height) {
			super(name, snappingHorizontal, snappingVertical, xOffset, yOffset, width, height);
		}

		@Override
		public boolean shouldRender() {
			if (!hud.inContainerMenu.isDefault()) return false;
			StandPower standPower = ClientPowerCache.getPower(PowerClass.STAND);
			return standPower != null && standPower.usesResolve();
		}
		
		@Override
		public void renderElement(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
			StandPower standPower = ClientPowerCache.getPower(PowerClass.STAND);
			float partialTick = ClientUtil.partialTick(deltaTracker, false);
			Minecraft mc = Minecraft.getInstance();
			
			int x = getX();
			int y = getY();
			int width = getWidth();
			int height = getHeight();
			
			GuiIcon emptySprite = HORIZONTAL_EMPTY;
			GuiIcon fullSprite = HORIZONTAL_FULL;
			
			float resolveMode = standPower.resolveHandler.getResolveModeTimerRatio(standPower, partialTick);
			if (resolveMode > 0) {
				BlitFloat.blitRadial(guiGraphics.pose(), mc, RESOLVE_MODE.file, 
						x + (width - RESOLVE_MODE.width) / 2, y + (height - RESOLVE_MODE.height) / 2, RESOLVE_MODE.width, RESOLVE_MODE.height, 0, 
						0, resolveMode, BlitFloat.NO_TINT);
			}
			
			float resolveRatio = standPower.getResolveRatio(partialTick);
			BlitFloat.blit(guiGraphics.pose(), mc, emptySprite.file, 
					x, y, width, height, 0, 
					BlitFloat.NO_TINT);
			float fillWidth = resolveRatio >= 1 ? width : Math.min(width * resolveRatio, width - 5);
			BlitFloat.blit(guiGraphics.pose(), mc, fullSprite.file, 
					x, y, fillWidth, height, 0, 
					0, 0, fillWidth, height, width, height, 
					BlitFloat.NO_TINT);
			
			if (resolveMode < 0) {
				float multiplier = standPower.resolveHandler.getTotalBoostVisible(standPower.getUser());
				if (multiplier > 1) {
					guiGraphics.drawCenteredString(mc.font, Component.literal("x" + multiplier), x + width / 2, y + 20, 0xFFA00000);
				}
			}
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
			if (!hud.inContainerMenu.isDefault()) return false;
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
			if (!hud.inContainerMenu.isDefault()) return false;
			StandEntity stand = ClientGlobals.playerStandEntity;
			return stand != null;
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
