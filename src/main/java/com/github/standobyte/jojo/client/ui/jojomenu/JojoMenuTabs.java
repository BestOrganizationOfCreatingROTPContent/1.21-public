package com.github.standobyte.jojo.client.ui.jojomenu;

import java.util.List;

import com.github.standobyte.jojo.client.ClientPowerCache;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.client.ClientTickHandler;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.client.standskin.StandSkinsScreen;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.power.ModPlayerPowers;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

// XXX (jojo menu) tab icons and names
public class JojoMenuTabs {
	public static void initDefaults() {}
	
	// Player menu
	
	public static final TabCategory CATEGORY_PLAYER_MENU = new TabCategory() {
		@Override
		public void renderIcon(GuiGraphics guiGraphics, int x, int y) {
			ClientUtil.renderPlayerFace(guiGraphics.pose(), x, y, Minecraft.getInstance().player);
		}
	}
			.withName(Component.translatable("jojo_ripples.ui.player_menu"));
	
	static {
		if (JojoMod.disableDevStuff()) {
			TabCategory.ALL_CATEGORIES.remove(CATEGORY_PLAYER_MENU);
		}
	}
	
	public static final Tab PLAYER_PROFILE = new Tab(CATEGORY_PLAYER_MENU) {
		@Override
		public void renderIcon(GuiGraphics guiGraphics, int x, int y) {
			ClientUtil.renderPlayerFace(guiGraphics.pose(), x, y, Minecraft.getInstance().player);
		}
	}
			.withName(Component.translatable(JojoMod.MOD_ID + ".menu.player.profile"));
	
	public static final Tab GROUP = new Tab(CATEGORY_PLAYER_MENU)
			.withName(Component.translatable(JojoMod.MOD_ID + ".menu.player.group"))
			.withIcon(new GuiIcon(JojoMod.resLoc("textures/gui/group.png"), 16, 16));
	
	public static final Tab STORY_ARCS = new Tab(CATEGORY_PLAYER_MENU)
			.withName(Component.translatable(JojoMod.MOD_ID + ".menu.player.story_arcs"))
			.withIcon(new GuiIcon(JojoMod.resLoc("textures/gui/arcs.png"), 16, 16));
	
	public static final Tab CLOTHES = new Tab(CATEGORY_PLAYER_MENU)
			.withName(Component.translatable(JojoMod.MOD_ID + ".menu.player.clothes"))
			.withIcon(new GuiIcon(JojoMod.resLoc("textures/gui/clothes.png"), 16, 16));
	
	// Stand
	
	public static final TabCategory CATEGORY_STAND = new TabCategory(PowerClass.STAND, null) {
		@Override
		public Component getName() {
			return Component.translatable("jojo_ripples.class.stand", ClientPowerCache.getPower(PowerClass.STAND).getName());
		}
		
		@Override
		public void renderIcon(GuiGraphics guiGraphics, int x, int y) {
			StandSkin skin = StandSkinsLoader.getCurSkin();
			if (skin != null) {
				this.icon = skin.getStandIcon();
				if (icon != null) {
					super.renderIcon(guiGraphics, x, y);
				}
			}
		}
	};
	
	public static final Tab STAND_INFO = new Tab(CATEGORY_STAND) {
		@Override
		public void renderIcon(GuiGraphics guiGraphics, int x, int y) {
			StandSkin skin = StandSkinsLoader.getCurSkin();
			if (skin != null) {
				this.icon = skin.getStandIcon();
				if (icon != null) {
					super.renderIcon(guiGraphics, x, y);
				}
			}
		}
	}
			.withName(Component.translatable(JojoMod.MOD_ID + ".menu.stand.info"));
	
	public static final Tab STAND_SKILLS = new Tab(CATEGORY_STAND)
			.withName(Component.translatable(JojoMod.MOD_ID + ".menu.stand.skills"))
			.withScreen(tab -> new StandSkillsScreen(Component.empty(), tab.category, tab))
			.withIcon(new GuiIcon(JojoMod.resLoc("textures/gui/stand_skills.png"), 16, 16));
	
	public static final Tab STAND_SKINS = new Tab(CATEGORY_STAND) {
		@Override
		public void renderIcon(GuiGraphics guiGraphics, int x, int y) {
			icon = null;
			StandPower standPower = ClientPowerCache.getPower(PowerClass.STAND);
			if (standPower != null && standPower.hasPower()) {
				ResourceLocation standId = standPower.getPowerType().getId();
				List<StandSkin> allSkins = StandSkinsLoader.getInstance().getStandSkinsView(standId);
				if (!allSkins.isEmpty()) {
					float ticks = ClientTickHandler.tickCount + ClientUtil.partialTick(Minecraft.getInstance().getTimer(), true);
					StandSkin cycledSkin = allSkins.get((int) (ticks / 20) % allSkins.size());
					icon = cycledSkin.getStandIcon();
				}
			}
			super.renderIcon(guiGraphics, x, y);
		}
	}
			.withName(Component.translatable(JojoMod.MOD_ID + ".menu.stand.skins"))
			.withScreen(tab -> {
				return PowerClass.STAND.getOptional(ClientProxy.getClientPlayer()).map(playerStand -> {
					return playerStand.hasPower() ? new StandSkinsScreen(playerStand) : null;
				}).orElse(null);
			});
	
	// Hamon
	
	public static final TabCategory CATEGORY_HAMON = new TabCategory(PowerClass.PLAYER_POWER, ModPlayerPowers.HAMON)
			.withName(Component.translatable("power." + JojoMod.MOD_ID + ".hamon"))
			.withIcon(new GuiIcon(JojoMod.resLoc("textures/power/hamon.png"), 16, 16));
	
	public static final Tab HAMON_INTRO = new Tab(CATEGORY_HAMON)
			.withName(Component.translatable("hamon.intro.tab"))
			.withIcon(new GuiIcon(JojoMod.resLoc("textures/power/hamon.png"), 16, 16));
	
	public static final Tab HAMON_STATS = new Tab(CATEGORY_HAMON) {
		@Override
		public void renderIcon(GuiGraphics guiGraphics, int x, int y) {
//			renderHamonStatsIcon();
		}
	}
			.withName(Component.translatable("hamon.stats.tab"));
	
	public static final Tab HAMON_STRENGTH_SKILLS = new Tab(CATEGORY_HAMON)
			.withName(Component.translatable("hamon.strength_skills.tab"))
			.withIcon(new GuiIcon(JojoMod.resLoc("textures/gui/hamon/skills_combat.png"), 16, 16));
	
	public static final Tab HAMON_CONTROL_SKILLS = new Tab(CATEGORY_HAMON)
			.withName(Component.translatable("hamon.control_skills.tab"))
			.withIcon(new GuiIcon(JojoMod.resLoc("textures/gui/hamon/skills_support.png"), 16, 16));
	
	public static final Tab HAMON_TECHNIQUES = new Tab(CATEGORY_HAMON) {
		@Override
		public void renderIcon(GuiGraphics guiGraphics, int x, int y) {
//			renderHamonTechniqueIcon();
		}
	}
			.withName(Component.translatable("hamon.techniques.tab"));
	
	// Vampirism

	public static final TabCategory CATEGORY_VAMPIRISM = new TabCategory(PowerClass.PLAYER_POWER, ModPlayerPowers.VAMPIRISM)
			.withName(Component.translatable("power." + JojoMod.MOD_ID + ".vampirism"))
			.withIcon(new GuiIcon(JojoMod.resLoc("textures/power/vampirism.png"), 16, 16));

	public static final Tab VAMPIRISM_SKILLS = new Tab(CATEGORY_VAMPIRISM)
			.withName(Component.translatable(JojoMod.MOD_ID + ".vampirism.skills"));
	
	// Controls
	
	public static final TabCategory CATEGORY_CONTROLS = new TabCategory()
			.withName(Component.translatable("jojo_ripples.screen.edit_hud_layout"))
			.withIcon(new GuiIcon(JojoMod.resLoc("textures/gui/controls.png"), 16, 16));
	
	public static final Tab STAND_POWER_CONTROLS = new Tab(CATEGORY_CONTROLS, PowerClass.STAND, null) {
		@Override
		public Component getName() {
			return Component.translatable("jojo_ripples.class.stand", ClientPowerCache.getPower(PowerClass.STAND).getName());
		}
		
		@Override
		public void renderIcon(GuiGraphics guiGraphics, int x, int y) {
			StandSkin skin = StandSkinsLoader.getCurSkin();
			if (skin != null) {
				this.icon = skin.getStandIcon();
				if (icon != null) {
					super.renderIcon(guiGraphics, x, y);
				}
			}
		}
	};
	
	public static final Tab PLAYER_POWER_CONTROLS = new Tab(CATEGORY_CONTROLS, PowerClass.PLAYER_POWER, null) {
		@Override
		public Component getName() {
			return Component.translatable("jojo_ripples.class.player_power", ClientPowerCache.getPower(PowerClass.PLAYER_POWER).getName());
		}
		
		@Override
		public void renderIcon(GuiGraphics guiGraphics, int x, int y) {
//			renderPlayerPowerIcon();
		}
	};

}
