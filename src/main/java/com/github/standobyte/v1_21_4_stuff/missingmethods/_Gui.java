package com.github.standobyte.v1_21_4_stuff.missingmethods;

import com.github.standobyte.jojo.client.ui.hud.VanillaHudSprites;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class _Gui {

    public static void renderAirBubbles(Gui gui, GuiGraphics guiGraphics, Player player, int vehicleMaxHealth, int y, int x) {
        int i3 = player.getMaxAirSupply();
        int j3 = Math.min(player.getAirSupply(), i3);
        if (player.isEyeInFluid(FluidTags.WATER) || j3 < i3) {
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
}
