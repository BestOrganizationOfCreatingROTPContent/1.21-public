package com.github.standobyte.jojo.client.entitycontrol.stand;

import java.util.Collection;
import java.util.List;

import com.github.standobyte.jojo.client.ClientPowerCache;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.client.ui.utils.ElementTransparency;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mixin.client.entitycontrol.GuiAccessor;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

import net.minecraft.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.event.EventHooks;

public class StandControlHud {
	protected static ResourceLocation ARMOR_FULL_SPRITE;
	protected static ResourceLocation ARMOR_HALF_SPRITE;
	protected static ResourceLocation ARMOR_EMPTY_SPRITE;
	protected static ResourceLocation HOTBAR_OFFHAND_LEFT_SPRITE;
	protected static ResourceLocation HOTBAR_OFFHAND_RIGHT_SPRITE;
	protected static ResourceLocation EFFECT_BACKGROUND_AMBIENT_SPRITE;
	protected static ResourceLocation EFFECT_BACKGROUND_SPRITE;

	private static void cacheSpritePaths(GuiAccessor gui) {
		if (ARMOR_FULL_SPRITE == null) {
			ARMOR_FULL_SPRITE = GuiAccessor.getARMOR_FULL_SPRITE();
			ARMOR_HALF_SPRITE = GuiAccessor.getARMOR_HALF_SPRITE();
			ARMOR_EMPTY_SPRITE = GuiAccessor.getARMOR_EMPTY_SPRITE();
			HOTBAR_OFFHAND_LEFT_SPRITE = GuiAccessor.getHOTBAR_OFFHAND_LEFT_SPRITE();
			HOTBAR_OFFHAND_RIGHT_SPRITE = GuiAccessor.getHOTBAR_OFFHAND_RIGHT_SPRITE();
			EFFECT_BACKGROUND_AMBIENT_SPRITE = GuiAccessor.getEFFECT_BACKGROUND_AMBIENT_SPRITE();
			EFFECT_BACKGROUND_SPRITE = GuiAccessor.getEFFECT_BACKGROUND_SPRITE();
		}
	}

	private long healthBlinkTime;
	private int lastHealth;
	private long lastHealthTime;
	private int displayHealth;
	
	public void renderHudElements(RenderGuiLayerEvent.Pre event, LivingEntity stand) {
		Minecraft mc = Minecraft.getInstance();
		ResourceLocation layerName = event.getName();
		GuiGraphics guiGraphics = event.getGuiGraphics();
		DeltaTracker deltaTracker = event.getPartialTick();
		GuiAccessor gui = (GuiAccessor) mc.gui;
		cacheSpritePaths(gui);
		if (layerName.equals(VanillaGuiLayers.PLAYER_HEALTH)) {
			float hpF = stand.getHealth();
			int hp = Mth.ceil(hpF);

			int tickCount = gui.getTickCount();
			RandomSource random = gui.getRandom();

			boolean renderHighlight = healthBlinkTime > (long) tickCount && (healthBlinkTime - (long) tickCount) / 3L % 2L == 1L;

			long time = Util.getMillis();
			if (hp < lastHealth) {
				lastHealthTime = time;
				healthBlinkTime = (long)(tickCount + 20);
			} else if (hp > lastHealth) {
				lastHealthTime = time;
				healthBlinkTime = (long)(tickCount + 10);
			}

			if (time - lastHealthTime > 1000L) {
				displayHealth = hp;
				lastHealthTime = time;
			}

			lastHealth = hp;
			int k = displayHealth;

			random.setSeed((long)(tickCount * 312871));
			int l = guiGraphics.guiWidth() / 2 - 91;
//			int i1 = guiGraphics.guiWidth() / 2 + 91;
			int j1 = guiGraphics.guiHeight() - mc.gui.leftHeight;
			float f = Math.max((float)stand.getAttributeValue(Attributes.MAX_HEALTH), (float)Math.max(k, hp));
			int k1 = Mth.ceil(stand.getAbsorptionAmount());
			int l1 = Mth.ceil((f + (float)k1) / 2.0F / 10.0F);
			int i2 = Math.max(10 - (l1 - 2), 3);
//			int j2 = j1 - 10;
			mc.gui.leftHeight += (l1 - 1) * i2 + 10;
			int k2 = -1;
			if (stand.hasEffect(MobEffects.REGENERATION)) {
				k2 = tickCount % Mth.ceil(f + 5.0F);
			}
			Profiler.get().push("health");
			renderHearts(gui, guiGraphics, stand, mc.player, l, j1, i2, k2, f, hp, k, k1, renderHighlight);
			Profiler.get().pop();
		}
		else if (layerName.equals(VanillaGuiLayers.ARMOR_LEVEL)) {
			int l = guiGraphics.guiWidth() / 2 - 91;
			Profiler.get().push("armor");
			renderArmor(guiGraphics, stand, guiGraphics.guiHeight() - mc.gui.leftHeight + 10, 1, 0, l);
			Profiler.get().pop();
			if (stand.getArmorValue() > 0) {
				mc.gui.leftHeight += 10;
			}
		}
		else if (layerName.equals(VanillaGuiLayers.FOOD_LEVEL)) {
			Player player = mc.player;
			if (player != null) {
				Profiler.get().push("food");
				int i1 = guiGraphics.guiWidth() / 2 + 91;
				int j1 = guiGraphics.guiHeight() - mc.gui.rightHeight;
				gui.invokeRenderFood(guiGraphics, player, j1, i1);
				mc.gui.rightHeight += 10;
				Profiler.get().pop();
			}
		}
		else if (layerName.equals(VanillaGuiLayers.AIR_LEVEL)) {
			Player player = mc.player;
			if (player != null) {
				int i1 = guiGraphics.guiWidth() / 2 + 91;
				int j2 = guiGraphics.guiHeight() - mc.gui.rightHeight;
				Profiler.get().push("air");
				gui.invokeRenderAirBubbles(guiGraphics, player, 10, j2, i1);
				Profiler.get().pop();
			}
		}
		else if (layerName.equals(VanillaGuiLayers.HOTBAR)) {
			ItemStack itemLeft = stand.getItemHeldByArm(HumanoidArm.LEFT);
			ItemStack itemRight = stand.getItemHeldByArm(HumanoidArm.RIGHT);

			int center = guiGraphics.guiWidth() / 2;
			int xLeft = center - 49;
			int xRight = center + 49;
			int y = guiGraphics.guiHeight() - 16 - 3;
			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate(0.0F, 0.0F, -90.0F);
			guiGraphics.blitSprite(RenderType::guiTextured, HOTBAR_OFFHAND_LEFT_SPRITE, xLeft - 29, y - 4, 29, 24);
			guiGraphics.blitSprite(RenderType::guiTextured, HOTBAR_OFFHAND_RIGHT_SPRITE, xRight, y - 4, 29, 24);

			guiGraphics.pose().popPose();

			if (!itemLeft.isEmpty()) renderSlot(guiGraphics, xLeft - 26, y, deltaTracker, stand, itemLeft, mc, 10);
			if (!itemRight.isEmpty()) renderSlot(guiGraphics, xRight + 10, y, deltaTracker, stand, itemRight, mc, 11);
		}
		else if (layerName.equals(VanillaGuiLayers.EFFECTS)) {
			Collection<MobEffectInstance> effects = stand.getActiveEffects();
			if (!effects.isEmpty() && (mc.screen == null || !mc.screen.showsActiveEffects())) {
				int beneficialI = 0;
				int harmfulI = 0;
				MobEffectTextureManager textureManager = mc.getMobEffectTextures();
				List<Runnable> list = Lists.newArrayListWithExpectedSize(effects.size());

				StandPower standPower = ClientPowerCache.getPower(PowerClass.STAND);
				StandSkin standSkin = StandSkinsLoader.getInstance().getSkin(standPower);
				int color = standSkin != null ? standSkin.getColor() : 0xFFFFFFFF;
				for (MobEffectInstance effect : Ordering.natural().reverse().sortedCopy(effects)) {
					var renderer = IClientMobEffectExtensions.of(effect);
					if (!renderer.isVisibleInGui(effect)) continue;
					Holder<MobEffect> holder = effect.getEffect();
					if (effect.showIcon()) {
						int x = guiGraphics.guiWidth();
						int y = 57;
						if (mc.isDemo()) {
							y += 15;
						}

						if (holder.value().isBeneficial()) {
							beneficialI++;
							x -= 25 * beneficialI;
						} else {
							harmfulI++;
							x -= 25 * harmfulI;
							y += 26;
						}

						float f = 1.0F;
						if (effect.isAmbient()) {
							guiGraphics.blitSprite(RenderType::guiTextured, EFFECT_BACKGROUND_AMBIENT_SPRITE, x, y, 24, 24, color /* someone tell the mapping creators this ain't blitOffset */);
						} else {
							guiGraphics.blitSprite(RenderType::guiTextured, EFFECT_BACKGROUND_SPRITE, x, y, 24, 24, color);
							if (effect.endsWithin(200)) {
								int i1 = effect.getDuration();
								int j1 = 10 - i1 / 20;
								f = Mth.clamp((float)i1 / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F)
										+ Mth.cos((float)i1 * (float) Math.PI / 5.0F) * Mth.clamp((float)j1 / 10.0F * 0.25F, 0.0F, 0.25F);
								f = Mth.clamp(f, 0.0F, 1.0F);
							}
						}

						if (renderer.renderGuiIcon(effect, mc.gui, guiGraphics, x, y, 0, f)) continue;
						TextureAtlasSprite textureatlassprite = textureManager.get(holder);
						int l1 = x;
						int k1 = y;
						float f1 = f;
						list.add(() -> {
							int i2 = ARGB.white(f1);
							guiGraphics.blitSprite(RenderType::guiTextured, textureatlassprite, l1 + 3, k1 + 3, 18, 18, i2);
						});
					}
				}

				list.forEach(Runnable::run);
			}
		}
	}

	// the fact that literally the same methods exist in the vanilla Gui class, but take a Player parameter when LivingEntity works just fine makes me irrationally angry
	private void renderHearts(GuiAccessor gui, 
			GuiGraphics guiGraphics, LivingEntity stand, Player player,
			int x, int y, int height, int offsetHeartIndex, float maxHealth,
			int currentHealth, int displayHealth, int absorptionAmount, boolean renderHighlight) {
		Gui.HeartType heartType;
		if (player.hasEffect(MobEffects.POISON)) {
			heartType = Gui.HeartType.POISIONED;
		} else if (player.hasEffect(MobEffects.WITHER)) {
			heartType = Gui.HeartType.WITHERED;
		} else if (player.isFullyFrozen()) {
			heartType = Gui.HeartType.FROZEN;
		} else {
			heartType = Gui.HeartType.NORMAL;
		}
		heartType = EventHooks.firePlayerHeartTypeEvent(player, heartType);

		boolean hardcore = player.level().getLevelData().isHardcore();
		int hpHearts = Mth.ceil((double)maxHealth / 2);
		int absorpHearts = Mth.ceil((double)absorptionAmount / 2);
		int healthInt = hpHearts * 2;

		for (int heart = hpHearts + absorpHearts - 1; heart >= 0; heart--) {
			int i1 = heart / 10;
			int j1 = heart % 10;
			int k1 = x + j1 * 8;
			int l1 = y - i1 * height;
			if (currentHealth + absorptionAmount <= 4) {
				l1 += gui.getRandom().nextInt(2);
			}

			if (heart < hpHearts && heart == offsetHeartIndex) {
				l1 -= 2;
			}

			gui.invokeRenderHeart(guiGraphics, Gui.HeartType.CONTAINER, k1, l1, hardcore, renderHighlight, false);
			int i2 = heart * 2;
			boolean flag1 = heart >= hpHearts;
			if (flag1) {
				int j2 = i2 - healthInt;
				if (j2 < absorptionAmount) {
					boolean blinking = j2 + 1 == absorptionAmount;
					gui.invokeRenderHeart(guiGraphics, heartType == Gui.HeartType.WITHERED ? heartType : Gui.HeartType.ABSORBING, k1, l1, hardcore, false, blinking);
				}
			}

			if (renderHighlight && i2 < displayHealth) {
				boolean blinking = i2 + 1 == displayHealth;
				gui.invokeRenderHeart(guiGraphics, heartType, k1, l1, hardcore, true, blinking);
			}

			if (i2 < currentHealth) {
				boolean blinking = i2 + 1 == currentHealth;
				gui.invokeRenderHeart(guiGraphics, heartType, k1, l1, hardcore, false, blinking);
			}
		}
	}

	public static void renderArmor(GuiGraphics guiGraphics, LivingEntity entity, int y, int heartRows, int height, int x) {
		int i = entity.getArmorValue();
		if (i > 0) {
			int j = y - (heartRows - 1) * height - 10;

			for (int k = 0; k < 10; k++) {
				int l = x + k * 8;
				if (k * 2 + 1 < i) guiGraphics.blitSprite(RenderType::guiTextured, ARMOR_FULL_SPRITE, l, j, 9, 9);
				if (k * 2 + 1 == i) guiGraphics.blitSprite(RenderType::guiTextured, ARMOR_HALF_SPRITE, l, j, 9, 9);
				if (k * 2 + 1 > i) guiGraphics.blitSprite(RenderType::guiTextured, ARMOR_EMPTY_SPRITE, l, j, 9, 9);
			}
		}
	}

	public static void renderSlot(GuiGraphics guiGraphics, int x, int y, DeltaTracker deltaTracker, LivingEntity entity, ItemStack stack, Minecraft mc, int seed) {
		if (!stack.isEmpty()) {
			float f = (float)stack.getPopTime() - deltaTracker.getGameTimeDeltaPartialTick(false);
			if (f > 0.0F) {
				float f1 = 1.0F + f / 5.0F;
				guiGraphics.pose().pushPose();
				guiGraphics.pose().translate((float)(x + 8), (float)(y + 12), 0.0F);
				guiGraphics.pose().scale(1.0F / f1, (f1 + 1.0F) / 2.0F, 1.0F);
				guiGraphics.pose().translate((float)(-(x + 8)), (float)(-(y + 12)), 0.0F);
			}

			guiGraphics.renderItem(entity, stack, x, y, seed);
			if (f > 0.0F) {
				guiGraphics.pose().popPose();
			}

			guiGraphics.renderItemDecorations(mc.font, stack, x, y);
		}
	}

	public ElementTransparency movementSpeedBarTranslucency = new ElementTransparency();
	public static final ResourceLocation SPEED_BAR_EMPTY = JojoMod.resLoc("textures/hud/stand_movement_speed_0.png");
	public static final ResourceLocation SPEED_BAR_FULL = JojoMod.resLoc("textures/hud/stand_movement_speed_1.png");
	public void renderExtraHud(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		if (movementSpeedBarTranslucency.shouldRender()) {
			float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
			float alpha = movementSpeedBarTranslucency.getAlpha(partialTick);
			int color = ARGB.colorFromFloat(alpha, 1, 1, 1);
			Minecraft mc = Minecraft.getInstance();
			int x = guiGraphics.guiWidth() / 2 + 4;
			int y = guiGraphics.guiHeight() / 2 - 8;

			BlitFloat.innerBlitFloat(guiGraphics, mc, RenderType.crosshair(SPEED_BAR_EMPTY),
					x, x + 16, y, y + 16,
					0, 1, 0, 1, 
					color);

			float speed = ClientStandController.manualMovementSpeed;
			if (speed > 1E-4) {
				float height = speed == 1 ? 1 : Math.min(speed, 1f - 1f / (16 * mc.options.guiScale().get()));
				BlitFloat.innerBlitFloat(guiGraphics, mc, RenderType.guiTextured(SPEED_BAR_FULL),
						x, x + 16, y + 16 * (1 - height), y + 16,
						0, 1, 1 - height, 1, 
						color);
			}
		}
	}

}
