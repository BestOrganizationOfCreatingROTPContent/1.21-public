package com.github.standobyte.jojo.client.jojomenu;

import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.client.standskin.StandSkinsScreen;
import com.github.standobyte.jojo.client.utils.GuiIcon;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.power.ModPlayerPowers;
import com.github.standobyte.jojo.powersystem.PowerClass;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

// XXX (jojo menu) tab icons and names
public class JojoMenuTabs {
	public static void initDefaults() {}
	
	private static final GuiIcon placeholder = new GuiIcon(JojoMod.resLoc("textures/power/hamon.png"), 0, 0, 16, 16, 16, 16);
	
	// Player menu
	
	public static final TabCategory CATEGORY_PLAYER_MENU = new TabCategory() {
		@Override
		public void renderIcon(GuiGraphics guiGraphics, int x, int y) {
//			renderPlayerFace();
		}
	}
			.withName(Component.translatable("jojo.ui.player_menu"));
	
	public static final Tab PLAYER_PROFILE = new Tab(CATEGORY_PLAYER_MENU) {
		@Override
		public void renderIcon(GuiGraphics guiGraphics, int x, int y) {
//			renderPlayerFace();
		}
	}
			.withName(Component.translatable(JojoMod.MOD_ID + ".menu.player.profile"));
	
	public static final Tab GROUP = new Tab(CATEGORY_PLAYER_MENU)
			.withName(Component.translatable(JojoMod.MOD_ID + ".menu.player.group"))
			.withIcon(/*groupIcon*/ placeholder);
	
	public static final Tab STORY_ARCS = new Tab(CATEGORY_PLAYER_MENU)
			.withName(Component.translatable(JojoMod.MOD_ID + ".menu.player.story_arcs"))
			.withIcon(/*storyArcsIcon*/ placeholder);
	
	public static final Tab CLOTHES = new Tab(CATEGORY_PLAYER_MENU)
			.withName(Component.translatable(JojoMod.MOD_ID + ".menu.player.clothes"))
			.withIcon(/*clothesIcon*/ placeholder);
	
	// Stand
	
	public static final TabCategory CATEGORY_STAND = new TabCategory(PowerClass.STAND, null) {
//		@Override
//		public Component getName() {
//			return getStandName();
//		}
		
		@Override
		public void renderIcon(GuiGraphics guiGraphics, int x, int y) {
//			renderStandIcon();
		}
	};
	
	public static final Tab STAND_INFO = new Tab(CATEGORY_STAND) {
		@Override
		public void renderIcon(GuiGraphics guiGraphics, int x, int y) {
//			renderStandIcon();
		}
	}
			.withName(Component.translatable(JojoMod.MOD_ID + ".menu.stand.info"));
	
	public static final Tab STAND_SKILLS = new Tab(CATEGORY_STAND)
			.withName(Component.translatable(JojoMod.MOD_ID + ".menu.stand.skills"))
			.withIcon(/*standSkillsIcon*/ placeholder);
	
	public static final Tab STAND_SKINS = new Tab(CATEGORY_STAND) {
		@Override
		public void renderIcon(GuiGraphics guiGraphics, int x, int y) {
//			renderStandSkinsIcons();
		}
	}
			.withName(Component.translatable(JojoMod.MOD_ID + ".menu.stand.skins"))
			.withScreen(() -> {
				return PowerClass.STAND.getOptional(ClientProxy.getClientPlayer()).map(playerStand -> {
					return playerStand.hasPower() ? new StandSkinsScreen(playerStand) : null;
				}).orElse(null);
			});
	
	// Hamon
	
	public static final TabCategory CATEGORY_HAMON = new TabCategory(PowerClass.PLAYER_POWER, ModPlayerPowers.HAMON)
			.withName(Component.translatable(JojoMod.MOD_ID + ".power.hamon"))
			.withIcon(/*hamonIcon*/ placeholder);
	
	public static final Tab HAMON_INTRO = new Tab(CATEGORY_HAMON)
			.withName(Component.translatable("hamon.intro.tab"))
			.withIcon(/*hamonIcon*/ placeholder);
	
	public static final Tab HAMON_STATS = new Tab(CATEGORY_HAMON) {
		@Override
		public void renderIcon(GuiGraphics guiGraphics, int x, int y) {
//			renderHamonStatsIcon();
		}
	}
			.withName(Component.translatable("hamon.stats.tab"));
	
	public static final Tab HAMON_STRENGTH_SKILLS = new Tab(CATEGORY_HAMON)
			.withName(Component.translatable("hamon.strength_skills.tab"))
			.withIcon(/*hamonStrengthSkillsIcon*/ placeholder);
	
	public static final Tab HAMON_CONTROL_SKILLS = new Tab(CATEGORY_HAMON)
			.withName(Component.translatable("hamon.control_skills.tab"))
			.withIcon(/*hamonControlSkillsIcon*/ placeholder);
	
	public static final Tab HAMON_TECHNIQUES = new Tab(CATEGORY_HAMON) {
		@Override
		public void renderIcon(GuiGraphics guiGraphics, int x, int y) {
//			renderHamonTechniqueIcon();
		}
	}
			.withName(Component.translatable("hamon.techniques.tab"));
	
	// Vampirism

	public static final TabCategory CATEGORY_VAMPIRISM = new TabCategory(PowerClass.PLAYER_POWER, ModPlayerPowers.VAMPIRISM)
			.withName(Component.translatable(JojoMod.MOD_ID + ".power.vampirism"))
			.withIcon(/*vampirismIcon*/ placeholder);

	public static final Tab VAMPIRISM_SKILLS = new Tab(CATEGORY_VAMPIRISM)
			.withName(Component.translatable(JojoMod.MOD_ID + ".vampirism.skills"));
	
	// Controls
	
	public static final TabCategory CATEGORY_CONTROLS = new TabCategory()
			.withName(Component.translatable("jojo.screen.edit_hud_layout"))
			.withIcon(/*controlsIcon*/ placeholder);
	
	public static final Tab STAND_POWER_CONTROLS = new Tab(CATEGORY_CONTROLS, PowerClass.STAND, null) {
//		@Override
//		public Component getName() {
//			return getStandName();
//		}
		
		@Override
		public void renderIcon(GuiGraphics guiGraphics, int x, int y) {
//			renderStandIcon();
		}
	};
	
	public static final Tab PLAYER_POWER_CONTROLS = new Tab(CATEGORY_CONTROLS, PowerClass.PLAYER_POWER, null) {
//		@Override
//		public Component getName() {
//			return getPlayerPowerName();
//		}
		
		@Override
		public void renderIcon(GuiGraphics guiGraphics, int x, int y) {
//			renderPlayerPowerIcon();
		}
	};

}
