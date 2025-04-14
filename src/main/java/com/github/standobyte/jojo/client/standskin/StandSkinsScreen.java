package com.github.standobyte.jojo.client.standskin;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.joml.Quaternionf;
import org.lwjgl.glfw.GLFW;

import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.client.entityrender.stand.StandEntityRenderer;
import com.github.standobyte.jojo.client.input.InputHandler;
import com.github.standobyte.jojo.core.packet.fromclient.ClSetStandSkinPacket;
import com.github.standobyte.jojo.client.jojomenu.IJojoMenuScreen;
import com.github.standobyte.jojo.client.jojomenu.JojoMenuTabs;
import com.github.standobyte.jojo.client.jojomenu.Tab;
import com.github.standobyte.jojo.client.jojomenu.TabCategory;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.standpower.StandInstance;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.EntityStandType;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;
import com.github.standobyte.jojo.util.MathUtil;
import com.github.standobyte.jojo.util.CommonEnums.Direction2D;
import com.google.common.collect.Streams;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;

// XXX stans skins UI
public class StandSkinsScreen extends Screen implements IJojoMenuScreen {
//	public static final ResourceLocation TEXTURE_MAIN_WINDOW = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/stand_skins.png");
//	private static final ResourceLocation TEXTURE_BG = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/stand_skins_bg.png");
	
	private static final int WINDOW_WIDTH = 230;
	private static final int WINDOW_HEIGHT = 180;
	private static final int WINDOW_INSIDE_X = 7;
	private static final int WINDOW_INSIDE_WIDTH = 201;
	private static final int WINDOW_INSIDE_Y = 20;
	private static final int WINDOW_INSIDE_HEIGHT = 153;
	
	private static final int SKINS_IN_ROW = 3;
	
	private static ResourceLocation latestStand = null;
	private static int latestScroll;
	
	private StandPower standCap;
	private List<SkinView> skins;
	private int tickCount = 0;
	private int scroll;
	private List<SkinView> skinsVisible;
	
	@Nullable
	private SkinFullView skinFullView;
	
	public StandSkinsScreen(StandPower power) {
		super(CommonComponents.EMPTY);
		setStandCap(power);
	}
	
	public static void openScreen() {
		PowerClass.STAND.getOptional(ClientProxy.getClientPlayer()).ifPresent(playerStand -> {
			if (playerStand.hasPower()) {
				StandSkinsScreen screen = new StandSkinsScreen(playerStand);
				Minecraft.getInstance().setScreen(screen);
			}
		});
	}
	
	private void setStandCap(StandPower standCap) {
		this.standCap = standCap;
		this.skins = Streams.mapWithIndex(StandSkinsLoader.getInstance()
				.getStandSkins(standCap.getPowerType().getId()), (skin, index) -> {
					int x = 12 + (int) (index % SKINS_IN_ROW) * (SkinView.boxWidth + 12);
					int y = 3 + (int) (index / SKINS_IN_ROW) * (SkinView.boxHeight + 3);
					return new SkinView(skin, x, y);
				})
				.collect(Collectors.toList());
		setScroll(0);
	}
	
	@Override
	public void init() {
	}
	
	@Override
	public TabCategory getTabCategory() {
		return JojoMenuTabs.CATEGORY_STAND;
	}
	
	@Override
	public Tab getTab() {
		return JojoMenuTabs.STAND_SKINS;
	}
	
	@Override
	public void tick() {
		tickCount++;
	}

	@Override
	public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		if (!standCap.hasPower()) {
			onClose();
			return;
		}
		
		renderBackground(gui, mouseX, mouseY, partialTick);
//		renderBgPattern(gui);
		renderContents(gui, mouseX, mouseY, partialTick);
//		renderWindow(gui);
//		
//		defaultRenderTabs(gui, mouseX, mouseY, this);
		
		renderTabs(gui, this);
		renderTabTooltip(gui, this, mouseX, mouseY);
		
		for (Renderable renderable : renderables) {
			renderable.render(gui, mouseX, mouseY, partialTick);
		}
	}
	
	private boolean isSkinSelected(StandSkin skin) {
		StandInstance stand = standCap.getStandInstance().get();
		return stand.getSelectedSkin().equals(skin.getNonDefaultId());
	}
	
//	private void renderBgPattern(GuiGraphics matrixStack) {
//		RenderSystem.pushMatrix();
//		RenderSystem.translatef(getWindowX() + 4, getWindowY() + 4, 0);
//		minecraft.getTextureManager().bind(TEXTURE_BG);
//		
//		int x = getWindowX() + WINDOW_INSIDE_X;
//		int y = getWindowY() + WINDOW_INSIDE_Y;
//		ClientUtil.enableGlScissor(x, y, WINDOW_INSIDE_WIDTH, WINDOW_INSIDE_HEIGHT);
//		int l = -scroll % 16;
//		for (int i1 = -1; i1 <= 12; ++i1) {
//			for (int j1 = -1; j1 <= 11; ++j1) {
//				blit(matrixStack, 5 + 16 * i1, l + 16 * j1, 0.0F, 0.0F, 16, 16, 16, 16);
//			}
//		}
//		ClientUtil.disableGlScissor();
//		
//		RenderSystem.popMatrix();
//	}
//	
//	private void renderWindow(MatrixStack matrixStack) {
//		RenderSystem.enableBlend();
//		minecraft.getTextureManager().bind(TEXTURE_MAIN_WINDOW);
//		blit(matrixStack, getWindowX(), getWindowY(), 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
//	}
	
	private void renderContents(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		int x = getWindowX() + WINDOW_INSIDE_X;
		int y = getWindowY() + WINDOW_INSIDE_Y;
		float ticks = tickCount + partialTick;
		gui.pose().pushPose();
		gui.pose().translate(x, y, 0);
		gui.enableScissor(0, 0, WINDOW_INSIDE_WIDTH, WINDOW_INSIDE_HEIGHT);
		if (skinFullView != null) {
			skinFullView.render(gui, mouseX, mouseY, ticks);
		}
		else {
			gui.pose().translate(0, -scroll, 0);
			for (SkinView skin : skinsVisible) {
				skin.renderStand(gui, mouseX, mouseY, ticks);
			}

			Optional<SkinView> hoveredSkin = getSkinAt(mouseX, mouseY);
			for (SkinView skin : skinsVisible) {
				skin.renderAdditional(gui, mouseX, mouseY, ticks, 
						hoveredSkin.map(hovered -> skin == hovered).orElse(false));
			}
		}
		gui.disableScissor();
		gui.pose().popPose();
	}
	
	private Optional<SkinView> getSkinAt(int mouseX, int mouseY) {
		if (skinFullView != null) return Optional.empty();
		
		int x = mouseX - (getWindowX() + WINDOW_INSIDE_X);
		int y = mouseY - (getWindowY() + WINDOW_INSIDE_Y);
		if (
				x >= 0 && x <= WINDOW_INSIDE_WIDTH && 
				y >= 0 && y <= WINDOW_INSIDE_HEIGHT) {
			int yWithScroll = y + scroll;
			
			return skins.stream().filter(skin -> 
			x		   > skin.x && x		   <= skin.x + SkinView.boxWidth && 
			yWithScroll > skin.y && yWithScroll <= skin.y + SkinView.boxHeight)
					.findFirst();
		}
		else {
			return Optional.empty();
		}
	}
	
	private boolean isSkinBoxVisible(SkinView skin) {
		int y = skin.y - scroll;
		return y + SkinView.boxHeight >= 0 && y < WINDOW_INSIDE_HEIGHT;
	}
	
	private int getWindowX() { return (width - WINDOW_WIDTH) / 2; }
	private int getWindowY() { return (height - WINDOW_HEIGHT) / 2; }
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		if (clickTab(mouseX, mouseY, mouseButton, this)) return true;
		
		Optional<SkinView> hoveredBox = getSkinAt((int) mouseX, (int) mouseY);
		if (skinFullView == null) {
			switch (mouseButton) {
				case GLFW.GLFW_MOUSE_BUTTON_1:
					if (hoveredBox.isPresent()) {
						selectSkin(hoveredBox.get().skin);
						return true;
					}
					break;
				case GLFW.GLFW_MOUSE_BUTTON_2:
					return hoveredBox.map(skinBox -> {
						setFullViewSkin(skinBox);
						return true;
					}).orElse(false);
			}
		}
		
		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double xOffset, double yOffset) {
		if (super.mouseScrolled(mouseX, mouseY, xOffset, yOffset)) {
			return true;
		}
		
		double scrollDelta = xOffset != 0 ? xOffset : yOffset;
		if (skinFullView != null) {
			return skinFullView.mouseScrolled(mouseX, mouseY, xOffset, yOffset);
		}
		else {
			addScroll((int) (-scrollDelta * 10));
		}
		
		return false;
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dragX, double dragY) {
		if (super.mouseDragged(mouseX, mouseY, mouseButton, dragX, dragY)) {
			return true;
		}
		
		if (skinFullView != null) {
			return skinFullView.mouseDragged(mouseX, mouseY, mouseButton, dragX, dragY);
		}
		
		return false;
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			if (skinFullView != null) {
				selectSkin(skinFullView.skin);
				return true;
			}
			else {
				
			}
		}
		
		else {
			Direction2D arrowKey = InputHandler.getArrowKey(keyCode);
			if (arrowKey != null) {
				if (skinFullView != null) {
					return skinFullView.keyPressed(arrowKey);
				}
				else {
					switch (arrowKey) {
						case RIGHT -> {}
						case DOWN -> {}
						case LEFT -> {}
						case UP -> {}
					}
					return true;
				}
			}
		}
		
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	@Override
	public void onClose() {
		if (skinFullView != null) {
			setFullViewSkin(null);
		}
		else {
			super.onClose();
		}
	}
	
	private void selectSkin(StandSkin skin) {
		PacketDistributor.sendToServer(new ClSetStandSkinPacket(skin.getNonDefaultId(), skin.standTypeId));
	}
	
	private void addScroll(int scroll) {
		setScroll(this.scroll + scroll);
	}
	
	private void setScroll(int scroll) {
		this.scroll = Mth.clamp(scroll, 0, getMaxScroll());
		this.skinsVisible = skins.stream()
				.filter(this::isSkinBoxVisible)
				.collect(Collectors.toList());
	}
	
	public int getMaxScroll() {
		int rowsCount = (skins.size() - 1) / SKINS_IN_ROW + 1;
		return Math.max((SkinView.boxHeight + 4) * rowsCount - WINDOW_INSIDE_HEIGHT, 0);
	}
	
	
	private void setFullViewSkin(@Nullable SkinView skin) {
		if (skin != null) {
			skinFullView = new SkinFullView(skin.skin, skins.indexOf(skin));
		}
		else {
			skinFullView = null;
		}
	}
	
	private class SkinView {
		public final StandSkin skin;
		public final int x;
		public final int y;
		public static final int boxWidth = 51;
		public static final int boxHeight = 72;
		
		public SkinView(StandSkin skin, int x, int y) {
			this.skin = skin;
			this.x = x;
			this.y = y;
		}
		
		public void renderStand(GuiGraphics gui, int mouseX, int mouseY, float ticks) {
//			minecraft.getTextureManager().bind(TEXTURE_MAIN_WINDOW);
//			blit(matrixStack, x, y, 98, 182, width, height);
			
			StandType standType = standCap.getPowerType();
			if (standType instanceof EntityStandType) {
				renderStandModel(gui, x + boxWidth / 2, y + boxHeight / 2 + 26.6667F, 25, 
						1, 0, 0, 0, 0, 
						(EntityStandType) standType, skin, ticks);
			}
		}
		
		public void renderAdditional(GuiGraphics gui, int mouseX, int mouseY, 
				float ticks, boolean isHovered) {
//			minecraft.getTextureManager().bind(TEXTURE_MAIN_WINDOW);
//			if (isSkinSelected(skin)) {
//				blit(matrixStack, x + boxWidth - 18, y + 2, 0, 192, 16, 16);
//			}
//			if (isHovered) {
//				float[] color = ClientUtil.rgb(skin.color);
//				RenderSystem.enableBlend();
//				RenderSystem.color4f(color[0], color[1], color[2], 1);
//				blit(matrixStack, x - 2, y - 2, 
//						32, 180, boxWidth + 4, boxHeight + 4);
//				RenderSystem.color4f(1, 1, 1, 1);
//				RenderSystem.disableBlend();
//			};
		}
	}
	
	
	private class SkinFullView {
		public final StandSkin skin;
		public final int skinIndex;
		public float yRot = 0;
		public float xRot = 0;
		public float scale = 1;
		public float xOffset = 0;
		public float yOffset = 0;
		
		public SkinFullView(StandSkin skin, int skinIndex) {
			this.skin = skin;
			this.skinIndex = skinIndex;
		}
		
		public void render(GuiGraphics gui, int mouseX, int mouseY, float ticks) {
//			ResourceLocation standIcon = JojoModUtil.makeTextureLocation("power", 
//					skinFullView.skin.standTypeId.getNamespace(), skinFullView.skin.standTypeId.getPath());
//			standIcon = skinFullView.skin.getRemappedResPath(standIcon).or(standIcon);
//			minecraft.getTextureManager().bind(standIcon);
//			blit(matrixStack, 4, 4, 0, 0, 16, 16, 16, 16);
			
			StandType standType = standCap.getPowerType();
			if (standType instanceof EntityStandType) {
				renderStandModel(gui, WINDOW_WIDTH / 2 - 15, 133.33F, 
						55, scale, yRot * MathUtil.PI, xRot * MathUtil.PI, xOffset, yOffset, 
						(EntityStandType) standType, skin, ticks);	
			}
			
//			if (isSkinSelected(skin)) {
//				minecraft.getTextureManager().bind(TEXTURE_MAIN_WINDOW);
//				blit(matrixStack, WINDOW_INSIDE_WIDTH - 20, 4, 0, 192, 16, 16);
//			}

			RenderSystem.enableBlend();
		}
		
		public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dragX, double dragY) {
			switch (mouseButton) {
				case GLFW.GLFW_MOUSE_BUTTON_1:
					this.yRot -= dragX / WINDOW_INSIDE_WIDTH * 2;
					this.xRot -= dragY / WINDOW_INSIDE_WIDTH * 2;
					break;
				case GLFW.GLFW_MOUSE_BUTTON_2:
					this.xOffset += dragX;
					this.yOffset += dragY;
					break;
			}
			return true;
		}
		
		public boolean mouseScrolled(double mouseX, double mouseY, double xOffset, double yOffset) {
			this.yRot += xOffset * 0.05F;
			float prevScale = this.scale;
			this.scale = Mth.clamp(this.scale + (float) yOffset * 0.05f * this.scale, 1, 20);
			
			if (yOffset < 0) {
				float offsetRatio = this.scale / prevScale; // good enough
				this.xOffset *= offsetRatio;
				this.yOffset *= offsetRatio;
			}
			
			return true;
		}
		
		public boolean keyPressed(Direction2D arrowKey) {
			SkinFullView prev = this;
			switch (arrowKey) {
				case DOWN, RIGHT -> setFullViewSkin(skins.get((this.skinIndex + 1) % skins.size()));
				case UP, LEFT -> setFullViewSkin(skins.get((this.skinIndex - 1 + skins.size()) % skins.size()));
			}
			StandSkinsScreen.this.skinFullView.copyFromPrev(prev);
			return true;
		}
		
		public void copyFromPrev(SkinFullView prev) {
			this.yRot = prev.yRot;
			this.xRot = prev.xRot;
			this.scale = prev.scale;
			this.xOffset = prev.xOffset;
			this.yOffset = prev.yOffset;
		}
	}

	public static void renderStandModel(GuiGraphics gui, float posX, float posY, 
			float scale, float scale2, float yRot, float xRot, float xOffsetRatio, float yOffsetRatio, 
			EntityStandType standType, StandSkin standSkin, float ticks) {
		Quaternionf rotation = new Quaternionf()
				.rotateX(-xRot)
				.rotateY(-yRot);
		
		gui.pose().pushPose();
		gui.pose().translate(posX, posY, 500.0);
		gui.pose().translate(xOffsetRatio, yOffsetRatio, 0);
		gui.pose().scale(scale, -scale, scale);
		gui.pose().translate(0, 1.25, 0);
		gui.pose().scale(scale2, scale2, scale2);
		gui.pose().mulPose(rotation);
		gui.pose().translate(0, -1.25, 0);
		gui.flush();
		Lighting.setupForEntityInInventory();
		EntityRenderDispatcher renderManager = Minecraft.getInstance().getEntityRenderDispatcher();
		
		StandEntityRenderer<?, ?, ?> renderer = (StandEntityRenderer<?, ?, ?>) renderManager.renderers.get(standType.getEntityType());
		renderManager.setRenderShadow(false);
		gui.drawSpecial(bufferSource -> renderer.renderWithRenderState(renderState -> {
			renderState.defaultSkin = StandSkinsLoader.getInstance().getDefaultSkin(standType.getId());
			renderState.skin = standSkin;
			renderState.standId = standType.getId();
			renderState.action.anim = StandEntityRenderer.IDLE_ANIM;
			renderState.action.phaseTime = ticks;
		}, gui.pose(), bufferSource, 0xF000F0));
		gui.flush();
		renderManager.setRenderShadow(true);
		gui.pose().popPose();
		
		Lighting.setupFor3DItems();
	}
}
