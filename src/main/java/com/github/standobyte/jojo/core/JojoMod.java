package com.github.standobyte.jojo.core;

import org.slf4j.Logger;

import com.github.standobyte.jojo.core.command.argument.ModCommandArguments;
import com.github.standobyte.jojo.init.ModBlocks;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModItemDataComponents;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModSoundEvents;
import com.github.standobyte.jojo.init.ModSpecialActions;
import com.github.standobyte.jojo.init.power.ModPlayerPowers;
import com.github.standobyte.jojo.init.power.ModStandAbilities;
import com.github.standobyte.jojo.init.power.ModStandEffects;
import com.github.standobyte.jojo.init.power.ModStands;
import com.github.standobyte.jojo.jojoimpl.hamon.ModHamonSkills;
import com.mojang.logging.LogUtils;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

// XXX allow PowerType to override controls/HUD rendering
// XXX make some sort of marker annotation for client-only methods in common classes
// XXX hardcode the stand credits (Map<ResourceLocation, Component[]>)
// TODO stuff for alpha testing:
//		entity action system
//		stand anims
//		base stand combat
// 		basic HUD
//		data-driven stands (/stand give)
//
//		clothes inventory and creative clothes items
//		importing emotes
//		importing clothes models (to test player animations & bends)
//		stand skins UI
@Mod(JojoMod.MOD_ID)
public class JojoMod {
	public static final String MOD_ID = "jojo_ripples";
	public static final Logger LOGGER = LogUtils.getLogger();

	public JojoMod(IEventBus modEventBus, ModContainer modContainer) {
		modEventBus.register(this);

		ModBlocks.BLOCKS.register(modEventBus);
		ModItems.ITEMS.register(modEventBus);
		ModItems.CREATIVE_MODE_TABS.register(modEventBus);
		ModEntityTypes.ENTITY_TYPES.register(modEventBus);
		ModDataAttachmentTypes.ATTACHMENT_TYPES.register(modEventBus);
		ModCommandArguments.ARGUMENT_TYPES.register(modEventBus);
		ModSoundEvents.SOUNDS.register(modEventBus);
		ModItemDataComponents.DATA_COMPONENT_TYPES.register(modEventBus);
		
		JojoRegistries.ABILITY_TYPES.register(modEventBus);
		ModStandAbilities.load();
		ModPlayerPowers.PLAYER_POWERS.register(modEventBus);
		ModHamonSkills.HAMON_SKILLS.register(modEventBus);
		ModHamonSkills.HAMON_CHARACTER_TECHNIQUES.register(modEventBus);
		ModStandEffects.STAND_EFFECT_TYPES.register(modEventBus);
		ModStands.DEFAULT_STANDS.register(modEventBus);
		ModSpecialActions.ACTIONS.register(modEventBus);
	}
	
	public static ResourceLocation resLoc(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
	
	@SubscribeEvent
	private void commonSetup(FMLCommonSetupEvent event) {
	}
	
	@SubscribeEvent
	private void registerNetwork(RegisterPayloadHandlersEvent event) {
		PacketsRegister.register(event);
	}
	
	public static Logger getLogger() {
		return LOGGER;
	}
	
	
	@Deprecated
	public static boolean disableDevStuff() {
		return FMLLoader.isProduction();
	}

}
