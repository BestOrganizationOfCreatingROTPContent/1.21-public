package com.github.standobyte.jojo.mechanics.entitycontrol.client.stand;

import java.util.Collection;
import java.util.List;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.ClientPowerCache;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.client.ui.hud.VanillaHudSprites;
import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.mixin.entitycontrol.client.GuiAccessor;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.v1_21_4_stuff.missingmethods.ARGB;
import com.github.standobyte.v1_21_4_stuff.missingmethods.Profiler;
import com.github.standobyte.v1_21_4_stuff.missingmethods._LivingEntity;
import com.github.standobyte.v1_21_4_stuff.missingmethods._Screen;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.Util;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.EventHooks;

public class StandHudElements {
	public static StandHudElements instance;
	public static void init() {
		if (instance == null) {
			instance = new StandHudElements();
			NeoForge.EVENT_BUS.register(instance);
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public void renderHudElements(RenderGuiLayerEvent.Pre event) {
		StandEntity stand = ClientGlobals.playerStandEntity;
		if (stand != null) {
			if (stand.isManuallyControlled()) {
				renderInManualControl(event, stand);
			}
			else {
				renderOutsideOfManualControl(event, stand);
			}
			renderAlways(event, stand);
		}
	}
	
	protected HealthHudTracker health = new HealthHudTracker();
	
	public static class HealthHudTracker {
		public long healthBlinkTime;
		public int lastHealth;
		public long lastHealthTime;
		public int displayHealth;
	}
	
	public void renderInManualControl(RenderGuiLayerEvent.Pre event, LivingEntity stand) {
		Minecraft mc = Minecraft.getInstance();
		ResourceLocation layerName = event.getName();
		GuiGraphics guiGraphics = event.getGuiGraphics();
		DeltaTracker deltaTracker = event.getPartialTick();
		GuiAccessor gui = (GuiAccessor) mc.gui;
		VanillaHudSprites.cacheSpritePaths(gui);
		if (layerName.equals(VanillaGuiLayers.PLAYER_HEALTH)) {
			if (mc.gameMode.canHurtPlayer()) {
				 renderHealth(stand, guiGraphics, gui, mc, health);
			}
		}
		else if (layerName.equals(VanillaGuiLayers.ARMOR_LEVEL)) {
			if (mc.gameMode.canHurtPlayer()) {
				renderArmor(stand, guiGraphics, gui, mc);
			}
		}
		else if (layerName.equals(VanillaGuiLayers.FOOD_LEVEL)) {
			if (mc.gameMode.canHurtPlayer() && mc.player != null) {
				renderFood(mc.player, guiGraphics, gui, mc);
			}
		}
		else if (layerName.equals(VanillaGuiLayers.AIR_LEVEL)) {
			if (mc.gameMode.canHurtPlayer() && mc.player != null) {
				renderAir(mc.player, guiGraphics, gui, mc);
			}
		}
		else if (layerName.equals(VanillaGuiLayers.HOTBAR)) {
			int center = guiGraphics.guiWidth() / 2;
			int xLeft = center;
			int xRight = center;
			renderStandHeldItems(stand, guiGraphics, gui, deltaTracker, mc, xLeft, xRight, true);
		}
	}
	
	public void renderOutsideOfManualControl(RenderGuiLayerEvent.Pre event, LivingEntity stand) {
		Minecraft mc = Minecraft.getInstance();
		ResourceLocation layerName = event.getName();
		GuiGraphics guiGraphics = event.getGuiGraphics();
		DeltaTracker deltaTracker = event.getPartialTick();
		GuiAccessor gui = (GuiAccessor) mc.gui;
		VanillaHudSprites.cacheSpritePaths(gui);
		if (layerName.equals(VanillaGuiLayers.HOTBAR)) {
			int center = guiGraphics.guiWidth() / 2;
			
            HumanoidArm offHand = mc.player != null ? mc.player.getMainArm().getOpposite() : HumanoidArm.LEFT;
            boolean attackIndicator = mc.options.attackIndicator().get() == AttackIndicatorStatus.HOTBAR;
			int xItemsCenter = switch (offHand) {
				case LEFT -> attackIndicator ? center + 167 : center + 141;
				case RIGHT -> attackIndicator ? center - 167 : center - 141;
			};
			
			renderStandHeldItems(stand, guiGraphics, gui, deltaTracker, mc, xItemsCenter, xItemsCenter, false);
		}
	}
	
	public void renderAlways(RenderGuiLayerEvent.Pre event, LivingEntity stand) {
		Minecraft mc = Minecraft.getInstance();
		ResourceLocation layerName = event.getName();
		GuiGraphics guiGraphics = event.getGuiGraphics();
		GuiAccessor gui = (GuiAccessor) mc.gui;
		VanillaHudSprites.cacheSpritePaths(gui);
		if (layerName.equals(VanillaGuiLayers.EFFECTS)) {
			renderStatusEffects(stand, guiGraphics, gui, mc, true);
		}
	}



	// the fact that literally the same methods exist in the vanilla Gui class, but take a Player parameter when LivingEntity works just fine makes me irrationally angry
	public static void renderHealth(LivingEntity stand, GuiGraphics guiGraphics, GuiAccessor gui, Minecraft mc, HealthHudTracker hudHp) {
		float hpF = stand.getHealth();
		int hp = Mth.ceil(hpF);

		int tickCount = gui.getTickCount();
		RandomSource random = gui.getRandom();

		boolean renderHighlight = hudHp.healthBlinkTime > (long) tickCount && (hudHp.healthBlinkTime - (long) tickCount) / 3L % 2L == 1L;

		long time = Util.getMillis();
		if (hp < hudHp.lastHealth) {
			hudHp.lastHealthTime = time;
			hudHp.healthBlinkTime = (long)(tickCount + 20);
		} else if (hp > hudHp.lastHealth) {
			hudHp.lastHealthTime = time;
			hudHp.healthBlinkTime = (long)(tickCount + 10);
		}

		if (time - hudHp.lastHealthTime > 1000L) {
			hudHp.displayHealth = hp;
			hudHp.lastHealthTime = time;
		}

		hudHp.lastHealth = hp;
		int k = hudHp.displayHealth;

		random.setSeed((long)(tickCount * 312871));
		int l = guiGraphics.guiWidth() / 2 - 91;
//		int i1 = guiGraphics.guiWidth() / 2 + 91;
		int j1 = guiGraphics.guiHeight() - mc.gui.leftHeight;
		float f = Math.max((float)stand.getAttributeValue(Attributes.MAX_HEALTH), (float)Math.max(k, hp));
		int k1 = Mth.ceil(stand.getAbsorptionAmount());
		int l1 = Mth.ceil((f + (float)k1) / 2.0F / 10.0F);
		int i2 = Math.max(10 - (l1 - 2), 3);
//		int j2 = j1 - 10;
		mc.gui.leftHeight += (l1 - 1) * i2 + 10;
		int k2 = -1;
		if (stand.hasEffect(MobEffects.REGENERATION)) {
			k2 = tickCount % Mth.ceil(f + 5.0F);
		}
		Profiler.get().push("health");
		renderHearts(gui, guiGraphics, stand, mc.player, l, j1, i2, k2, f, hp, k, k1, renderHighlight);
		Profiler.get().pop();
	}
	
	public static void renderHearts(GuiAccessor gui, 
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
	
	public static void renderArmor(LivingEntity stand, GuiGraphics guiGraphics, GuiAccessor gui, Minecraft mc) {
		int l = guiGraphics.guiWidth() / 2 - 91;
		Profiler.get().push("armor");
		renderArmor(guiGraphics, stand, guiGraphics.guiHeight() - mc.gui.leftHeight + 10, 1, 0, l);
		Profiler.get().pop();
		if (stand.getArmorValue() > 0) {
			mc.gui.leftHeight += 10;
		}
	}

	public static void renderArmor(GuiGraphics guiGraphics, LivingEntity entity, int y, int heartRows, int height, int x) {
		int i = entity.getArmorValue();
		if (i > 0) {
			int j = y - (heartRows - 1) * height - 10;

			for (int k = 0; k < 10; k++) {
				int l = x + k * 8;
				if (k * 2 + 1 < i) guiGraphics.blitSprite(/*RenderType::guiTextured, */VanillaHudSprites.ARMOR_FULL_SPRITE, l, j, 9, 9);
				if (k * 2 + 1 == i) guiGraphics.blitSprite(/*RenderType::guiTextured, */VanillaHudSprites.ARMOR_HALF_SPRITE, l, j, 9, 9);
				if (k * 2 + 1 > i) guiGraphics.blitSprite(/*RenderType::guiTextured, */VanillaHudSprites.ARMOR_EMPTY_SPRITE, l, j, 9, 9);
			}
		}
	}
	
	public static void renderFood(Player player, GuiGraphics guiGraphics, GuiAccessor gui, Minecraft mc) {
		Profiler.get().push("food");
		int i1 = guiGraphics.guiWidth() / 2 + 91;
		int j1 = guiGraphics.guiHeight() - mc.gui.rightHeight;
		gui.invokeRenderFood(guiGraphics, player, j1, i1);
		mc.gui.rightHeight += 10;
		Profiler.get().pop();
	}
	
	public static void renderAir(LivingEntity player, GuiGraphics guiGraphics, GuiAccessor gui, Minecraft mc) {
		int x = guiGraphics.guiWidth() / 2 + 91;
		int y = guiGraphics.guiHeight() - mc.gui.rightHeight;
		Profiler.get().push("air");
		renderAirBubbles(mc.gui, guiGraphics, player, 10, y, x);
		Profiler.get().pop();
	}

    public static void renderAirBubbles(Gui gui, GuiGraphics guiGraphics, LivingEntity player, int vehicleMaxHealth, int y, int x) {
        int i3 = player.getMaxAirSupply();
        int j3 = Math.min(player.getAirSupply(), i3);
        if (player.isEyeInFluid(FluidTags.WATER) && player.canDrownInFluidType(NeoForgeMod.WATER_TYPE.value()) || j3 < i3) {
            int l3 = Mth.ceil((double)(j3 - 2) * 10.0 / (double)i3);
            int i4 = Mth.ceil((double)j3 * 10.0 / (double)i3) - l3;
            RenderSystem.enableBlend();

            for (int j4 = 0; j4 < l3 + i4; j4++) {
                if (j4 < l3) {
                	guiGraphics.blitSprite(VanillaHudSprites.AIR_SPRITE, x - j4 * 8 - 9, y, 9, 9);
                } else {
                	guiGraphics.blitSprite(VanillaHudSprites.AIR_BURSTING_SPRITE, x - j4 * 8 - 9, y, 9, 9);
                }
            }

            RenderSystem.disableBlend();
            gui.rightHeight += 10;
        }
    }
	
	public static void renderStatusEffects(LivingEntity stand, GuiGraphics guiGraphics, GuiAccessor gui, Minecraft mc, boolean standColor) {
		Collection<MobEffectInstance> effects = stand.getActiveEffects();
		if (!effects.isEmpty() && (mc.screen == null || !_Screen.showsActiveEffects(mc.screen))) {
			int beneficialI = 0;
			int harmfulI = 0;
			MobEffectTextureManager textureManager = mc.getMobEffectTextures();
			List<Runnable> list = Lists.newArrayListWithExpectedSize(effects.size());

			int color = 0xFFFFFFFF;
			if (standColor) {
				StandPower standPower = ClientPowerCache.getPower(PowerClass.STAND);
				StandSkin standSkin = StandSkinsLoader.getInstance().getSkin(standPower);
				if (standSkin != null) {
					color = standSkin.getColor();
				}
			}
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

					float alpha = 1.0F;
					ResourceLocation effectBgSprite = effect.isAmbient() ? VanillaHudSprites.EFFECT_BACKGROUND_AMBIENT_SPRITE : VanillaHudSprites.EFFECT_BACKGROUND_SPRITE;
					// XXX stand status effect background sprite - color doesn't work
					BlitFloat.blit(guiGraphics.pose(), mc, mc.getGuiSprites().getSprite(effectBgSprite), x, y, 24, 24, 0, color);
					if (!effect.isAmbient()) {
						if (effect.endsWithin(200)) {
							int i1 = effect.getDuration();
							int j1 = 10 - i1 / 20;
							alpha = Mth.clamp((float)i1 / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F)
									+ Mth.cos((float)i1 * (float) Math.PI / 5.0F) * Mth.clamp((float)j1 / 10.0F * 0.25F, 0.0F, 0.25F);
							alpha = Mth.clamp(alpha, 0.0F, 1.0F);
						}
					}

					if (renderer.renderGuiIcon(effect, mc.gui, guiGraphics, x, y, 0, alpha)) continue;
					TextureAtlasSprite textureatlassprite = textureManager.get(holder);
					int l1 = x;
					int k1 = y;
					float f1 = alpha;
					list.add(() -> {
						int i2 = ARGB.white(f1);
						BlitFloat.blit(guiGraphics.pose(), mc, textureatlassprite,
								l1 + 3, k1 + 3, 18, 18, 0, 
								i2);
					});
				}
			}

			list.forEach(Runnable::run);
		}
	}
	
	
	
	private void renderStandHeldItems(LivingEntity stand, GuiGraphics guiGraphics, GuiAccessor gui, DeltaTracker deltaTracker, Minecraft mc, 
			int xLeft, int xRight, boolean renderEmpty) {
		ItemStack itemLeft = _LivingEntity.getItemHeldByArm(stand, HumanoidArm.LEFT);
		ItemStack itemRight = _LivingEntity.getItemHeldByArm(stand, HumanoidArm.RIGHT);
		if (!renderEmpty && itemLeft.isEmpty() && itemRight.isEmpty()) return;

		int y = guiGraphics.guiHeight() - 16 - 3;
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(0.0F, 0.0F, -90.0F);
		guiGraphics.blitSprite(/*RenderType::guiTextured, */VanillaHudSprites.HOTBAR_OFFHAND_LEFT_SPRITE, xLeft - 29 + 7, y - 4, 29, 24);
		guiGraphics.blitSprite(/*RenderType::guiTextured, */VanillaHudSprites.HOTBAR_OFFHAND_RIGHT_SPRITE, xRight - 7, y - 4, 29, 24);

		guiGraphics.pose().popPose();

		if (!itemLeft.isEmpty()) renderSlot(guiGraphics, xLeft - 26 + 7, y, deltaTracker, stand, itemLeft, mc, 10);
		if (!itemRight.isEmpty()) renderSlot(guiGraphics, xRight + 10 - 7, y, deltaTracker, stand, itemRight, mc, 11);
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

}
