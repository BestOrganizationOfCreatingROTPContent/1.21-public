package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mc.item.DebugItem;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
	public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, JojoMod.MOD_ID);
	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(JojoMod.MOD_ID);
	
	public static final DeferredItem<Item> DEBUG_ITEM = ITEMS.registerItem("debug_item", DebugItem::new, new Item.Properties());
	
	
	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB = CREATIVE_MODE_TABS.register("jojo_rotp_main", () -> CreativeModeTab.builder()
			.title(Component.translatable("itemGroup.jojo_rotp.main"))
			.icon(() -> Items.STICK.getDefaultInstance())
			.displayItems((parameters, output) -> {
			}).build());
	
	
	
}
