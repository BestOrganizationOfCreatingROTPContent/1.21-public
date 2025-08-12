package com.github.standobyte.jojo.client.ui.hud.marker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.standpower.StandEffectInstance;
import com.github.standobyte.jojo.util.MathUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public abstract class MarkerRenderer {
//	protected ResourceLocation iconTexture;
//	protected Ability iconAbility;
	private final List<MarkerInstance> positions = new ArrayList<>();
	// somewhere between 1.21.2 and 1.21.4, the markers stop rendering through blocks except in fabulous mode
	protected boolean renderThroughBlocks = true;
	protected final Minecraft mc = Minecraft.getInstance();
	
	public static void registerMarkerRenderer(MarkerRenderer markerRenderer) {
		MarkerRenderer.Handler.RENDERERS.add(markerRenderer);
	}
	

//	@Deprecated
//	/**
//	 * @deprecated use {@link MarkerRenderer#MarkerRenderer(ResourceLocation, Action, Minecraft)}
//	 */
//	public MarkerRenderer(int color, ResourceLocation iconTexture, Minecraft mc) {
//		this(iconTexture, mc);
//	}
//
//	public MarkerRenderer(ResourceLocation iconTexture, Minecraft mc) {
//		this(iconTexture, null, mc);
//	}
//
//	public MarkerRenderer(ResourceLocation defaultIconTexture, Ability iconAbility, Minecraft mc) {
//		this.iconTexture = defaultIconTexture;
//		this.iconAbility = iconAbility;
//		this.mc = mc;
//	}

	protected void render(PoseStack poseStack, Camera camera, float partialTick) {
		if (shouldRender()) {
			positions.clear();
			updatePositions(positions, partialTick);
			if (!positions.isEmpty()) {
				poseStack.pushPose();
				poseStack.mulPose(camera.rotation());
				poseStack.mulPose(Axis.ZP.rotationDegrees(camera.getRoll()));
				poseStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
				poseStack.mulPose(Axis.YP.rotationDegrees(camera.getYRot()));

				positions.forEach(marker -> {
					if (renderThroughBlocks) {
						RenderSystem.disableDepthTest();
					} else {
						RenderSystem.enableDepthTest();
					}
					Vec3 diff = marker.pos.subtract(camera.getPosition())
							.yRot(camera.getYRot() * MathUtil.DEG_TO_RAD)
							.xRot(camera.getXRot() * MathUtil.DEG_TO_RAD)
							.zRot(camera.getRoll() * MathUtil.DEG_TO_RAD);
					renderAt(poseStack, marker, camera, diff, partialTick, getColor());
				});
				RenderSystem.enableDepthTest();

				poseStack.popPose();
			}
		}
	}

	protected void renderAt(PoseStack poseStack, MarkerInstance marker, Camera camera, Vec3 diff, float partialTick, int color) {
		poseStack.pushPose();

		double distance = diff.length();
		if (distance > 256) return;

		float scale = Math.min((float) Math.pow(2, (16 - Math.min(distance, 32)) / 16) * (float) distance / 256, 1);

		poseStack.translate(diff.x, diff.y, diff.z);
		poseStack.scale(-scale, -scale, 1);
		poseStack.scale(0.8f, 0.8f, 0.8f);

		poseStack.pushPose();
		poseStack.translate(-8, -28, 0);
		renderIcon(poseStack, marker, partialTick);
		poseStack.popPose();
		renderBorder(poseStack, marker, partialTick, color);

		poseStack.pushPose();
		poseStack.translate(-8, -28, 0);
		RenderSystem.disableDepthTest();
		renderIconOnBorder(poseStack, marker, partialTick);
		RenderSystem.enableDepthTest();
		poseStack.popPose();

		poseStack.popPose();
	}

	// XXX (marker) icons
	protected void renderIcon(PoseStack poseStack, MarkerInstance marker, float partialTick) {
//		ResourceLocation icon = getIcon();
//		if (icon != null) {
//			mc.getTextureManager().bind(icon);
//			AbstractGui.blit(poseStack, 0, 0, 0, 0, 16, 16, 16, 16);
//		}
	}
	
	public static final GuiIcon MARKER_BORDER = new GuiIcon(JojoMod.resLoc("textures/hud/marker.png"), 32, 32);
	public static final GuiIcon MARKER_BORDER_OUTLINE = new GuiIcon(JojoMod.resLoc("textures/hud/marker_highlight.png"), 32, 32);

	protected void renderBorder(PoseStack poseStack, MarkerInstance marker, float partialTick, int color) {
        MARKER_BORDER.render(poseStack, -16, -32, color);
        if (marker.outlined) {
        	MARKER_BORDER_OUTLINE.render(poseStack, -16, -32);
        }
	}

	protected void renderIconOnBorder(PoseStack poseStack, MarkerInstance marker, float partialTick) {}

	protected abstract boolean shouldRender();
	protected abstract void updatePositions(List<MarkerInstance> list, float partialTick);

	// XXX (marker) UI color (current stand color)
	protected int getColor() {
		return 0xFFFFFFFF;
	}

	@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
	public static class Handler {
		private static Collection<MarkerRenderer> RENDERERS = new ArrayList<>();

		@SubscribeEvent(priority = EventPriority.LOW)
		public static void renderMarkers(RenderLevelStageEvent event) {
			if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
				Minecraft mc = Minecraft.getInstance();
				if (!mc.options.hideGui) {
					RenderSystem.disableDepthTest();
					// XXX (marker) fabulous graphics fix
//					if (mc.options.graphicsMode().get() == GraphicsStatus.FABULOUS) { // it just works
//						RenderSystem.enableTexture();
//					}

					float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
					RENDERERS.forEach(marker -> marker.render(event.getPoseStack(), event.getCamera(), partialTick));

					mc.renderBuffers().bufferSource().endBatch();
					RenderSystem.enableDepthTest();
				}
			}
		}
	}



	protected static class MarkerInstance {
		protected Vec3 pos;
		protected boolean outlined;
		protected final Optional<StandEffectInstance> standEffect;

		public MarkerInstance(Vec3 pos) {
			this(pos, false);
		}

		public MarkerInstance(Vec3 pos, boolean outlined) {
			this(pos, outlined, Optional.empty());
		}

		public MarkerInstance(Vec3 pos, boolean outlined, Optional<StandEffectInstance> standEffect) {
			this.pos = pos;
			this.outlined = outlined;
			this.standEffect = standEffect;
		}
	}
}
