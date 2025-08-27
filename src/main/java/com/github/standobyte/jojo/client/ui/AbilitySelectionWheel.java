package com.github.standobyte.jojo.client.ui;

import java.util.ArrayList;
import java.util.List;

import com.github.standobyte.jojo.client.ClientPowerCache;
import com.github.standobyte.jojo.client.input.InputHandler;
import com.github.standobyte.jojo.client.input.controlscheme.ClientControlScheme;
import com.github.standobyte.jojo.client.input.controlscheme.ClientControlScheme.HotbarSlot;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.client.ui.powerhud.tooltip.TooltipParams;
import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.Moveset;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.v1_21_4_stuff.missingmethods.ARGB;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.settings.KeyModifier;

public class AbilitySelectionWheel extends Screen implements ScreenLetsUseWASD {
	protected static final ResourceLocation DEFAULT_TEXTURE = JojoMod.resLoc("textures/ability_wheel.png");
	protected ResourceLocation texture;
	public ClientControlScheme.Hotbar abilities;
	public Power<?> power;
	public Moveset moveset;

	public AbilitySelectionWheel(ClientControlScheme.Hotbar abilities, Power<?> power, Moveset moveset) {
		super(Component.translatable("jojo.screen.ability_selection_wheel"));
		this.abilities = abilities;
		this.power = power;
		this.moveset = moveset;
		
		StandPower standPower = ClientPowerCache.getPower(PowerClass.STAND);
		if (standPower != null) {
			StandSkinsLoader skinLoader = StandSkinsLoader.getInstance();
			StandSkin skin = skinLoader.getSkin(standPower);
			if (skin != null) {
				texture = skin.getTexture(DEFAULT_TEXTURE);
			}
		}
		if (texture == null) {
			texture = DEFAULT_TEXTURE;
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		if (abilities == null || !InputHandler.getInstance().isHeld(abilities.switchAbilityKey, null)) {
			pickAbilityAt(mouseX, mouseY);
			onClose();
			return;
		}
		super.render(guiGraphics, mouseX, mouseY, partialTick);

		float width = 256;
		float height = 256;
		float x = (this.width - width) / 2f;
		float y = (this.height - height) / 2f;
		PoseStack pose = guiGraphics.pose();
		ResourceLocation texture = DEFAULT_TEXTURE;
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		int hovered = getSlotIndexAt(mouseX, mouseY);
		int n = abilities.slots.size();
		float angle0 = 0;
		float fill = 1f / n;
		float angleStep = 2 * (float) Math.PI * fill;
		for (int i = 0; i < n; i++) {
			boolean highlight = i == hovered;
			float alpha = highlight ? 0.5f : 0.25f;;
			if (highlight) {
				pose.pushPose();
				pose.translate(this.width / 2, this.height / 2, 0);
				pose.scale(1.1f, 1.1f, 1);
				pose.translate(-this.width / 2, -this.height / 2, 10);
			}
			BlitFloat.blitRadial(pose, Minecraft.getInstance(), texture, 
					x, y, width, height, 0, 
					angle0, fill, ARGB.white(alpha));
			angle0 += angleStep;
			if (highlight) {
				pose.popPose();
			}
		}
		RenderSystem.disableBlend();
		
		abilityNames.clear();
		if (hovered != -1 && moveset != null) {
			HotbarSlot slot = abilities.slots.get(hovered);
			KeyModifier curModifier = InputHandler.getInstance().getCurModifier();
			for (InputMethod inputMethod : InputMethod.values()) {
				var byInputMethod = slot.binds.get(inputMethod);
				if (byInputMethod != null) {
					String abilityName = byInputMethod.get(curModifier);
					if (abilityName != null) {
						Ability ability = moveset.getAbility(abilityName);
						if (ability != null) {
							abilityNames.add(ability.getName(power).copy().withStyle(ChatFormatting.BLACK));
						}
					}
				}
			}
		}
		if (!abilityNames.isEmpty()) {
			TooltipParams.set(TooltipParams.paperStyle());
			guiGraphics.renderComponentTooltip(font, abilityNames, mouseX, mouseY);
		}
	}
	List<Component> abilityNames = new ArrayList<>(2);

	public int getSlotIndexAt(int mouseX, int mouseY) {
		if (abilities == null) return -1;
		int xCenter = this.width / 2;
		int yCenter = this.height / 2;
		if (xCenter == mouseX && yCenter == mouseY) {
			return -1;
//			return abilities.slots.size() > abilities.slotIndex ? abilities.slotIndex : -1;
		}
		int xOffs = mouseX - xCenter;
		int yOffs = mouseY - yCenter;
		double angle = Mth.atan2(xOffs, -yOffs); // [0; 2*PI)
		if (angle < 0) angle += 2 * Math.PI;
		if (angle >= 2 * Math.PI) angle -= 2 * Math.PI;
		int index = Mth.floor(abilities.slots.size() * angle / (2 * Math.PI));
		return index;
	}
	
	protected boolean pickAbilityAt(int mouseX, int mouseY) {
		if (abilities != null) {
			int clickedIndex = getSlotIndexAt(mouseX, mouseY);
			if (clickedIndex >= 0 && clickedIndex < abilities.slots.size()) {
				abilities.slotIndex = clickedIndex;
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (super.mouseClicked(mouseX, mouseY, button)) return true;
		
		if (pickAbilityAt((int) mouseX, (int) mouseY)) {
			onClose();
			return true;
		}
		
		return false;
	}




	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}

}
