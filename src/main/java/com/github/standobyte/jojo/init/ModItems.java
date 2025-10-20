package com.github.standobyte.jojo.init;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.mc.item.DebugItem;
import com.github.standobyte.jojo.mc.item.StandDiscItem;
import com.github.standobyte.jojo.mc.item.component.StandWrittenOnDisc;
import com.github.standobyte.jojo.mechanics.StoryPart;
import com.github.standobyte.jojo.mechanics.clothes.ClothesItem;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesDataComponent;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesPiece;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesSet;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesSlotType;
import com.github.standobyte.jojo.mechanics.clothes.mannequin.MannequinItem;
import com.github.standobyte.jojo.powersystem.standpower.StandInstance;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public final class ModItems {
	public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, JojoMod.MOD_ID);
	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(JojoMod.MOD_ID);

	public static final DeferredItem<Item> DEBUG_ITEM = ITEMS.registerItem("debug_item", DebugItem::new, new Item.Properties());

	public static final DeferredItem<Item> STAND_DISC = ITEMS.registerItem("stand_disc", StandDiscItem::new, new Item.Properties().stacksTo(1));

	public static final DeferredItem<Item> MANNEQUIN = ITEMS.registerItem("mannequin", props -> new MannequinItem(props, false), new Item.Properties().stacksTo(16));

	public static final DeferredItem<Item> MANNEQUIN_SLIM = ITEMS.registerItem("mannequin_slim", props -> new MannequinItem(props, true), new Item.Properties().stacksTo(16));

	public static final DeferredItem<ClothesItem> CLOTHES_BASE_ITEM = ITEMS.registerItem("clothes", props -> new ClothesItem(props));

	
	public static Comparator<StandInstance> discsOrder(HolderLookup.Provider registries) {
		return Comparator
				.comparingInt((StandInstance stand) -> stand.getStandType().discCategoryPriority)
				.thenComparing((StandInstance stand) -> StoryPart.getStoryPart(stand, registries), StoryPart.COMPARATOR)
				.thenComparingInt((StandInstance stand) -> stand.getStandType().discStoryPartPriority);
	}

	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB = CREATIVE_MODE_TABS.register(JojoMod.MOD_ID + "_main", () -> CreativeModeTab.builder()
			.title(Component.translatable("itemGroup." + JojoMod.MOD_ID + "_main"))
			.icon(() -> DEBUG_ITEM.value().getDefaultInstance())
			.displayItems((CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) -> {
				// most of the mod's items
				Stream<StandType> stands = StandType.getAllEnabledStands();
				stands
				.map(StandInstance::new)
				.sorted(discsOrder(parameters.holders()))
				.map((StandInstance stand) -> {
					ItemStack disc = new ItemStack(STAND_DISC.get());
					disc.set(ModItemDataComponents.DISC_STAND.get(), new StandWrittenOnDisc(stand));
					return disc;
				})
				.forEach(item -> output.accept(item, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS));
			}).build());

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void addToModCreativeTabLast(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == MAIN_TAB.getKey()) {
			// items related to clothes
			event.accept(MANNEQUIN.get());
			event.accept(MANNEQUIN_SLIM.get());

			// the clothes items themselves
			ClothesItem clothesFactory = CLOTHES_BASE_ITEM.get();

			event.getParameters().holders()
			.lookup(JojoRegistries.CLOTHES_SETS_REG_KEY)
			.ifPresent(
					clothesSets -> clothesSets.listElements()
					.flatMap(setHolder -> {
						List<ClothesDataComponent> components = new ArrayList<>(ClothesSlotType.values().length);
						ClothesSet set = setHolder.value();
						for (ClothesSlotType slot : ClothesSlotType.values()) {
							ClothesPiece piece = set.getPiece(slot);
							if (piece != null) {
								components.add(new ClothesDataComponent(setHolder, slot, ClothesPiece.SubClothingPiece.FULL));
							}
						}
						return components.stream();
					})
					.map(clothesFactory::makeClothesPieceStack)
					.forEach(item -> event.accept(item, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS)));
		}
	}

}
