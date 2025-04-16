package com.github.standobyte.jojo.init;

import java.util.function.Supplier;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mc.item.component.StandWrittenOnDisc;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesDataComponent;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItemDataComponents {
	public static final DeferredRegister.DataComponents DATA_COMPONENT_TYPES = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, JojoMod.MOD_ID);
	
	public static final Supplier<DataComponentType<StandWrittenOnDisc>> DISC_STAND = DATA_COMPONENT_TYPES.registerComponentType("disc_stand", 
			builder -> builder
			.persistent(StandWrittenOnDisc.CODEC)
			.networkSynchronized(StandWrittenOnDisc.STREAM_CODEC)
			.cacheEncoding());

	public static final Supplier<DataComponentType<ClothesDataComponent>> CLOTHES_PIECE = DATA_COMPONENT_TYPES.registerComponentType("clothes", 
			builder -> builder
			.persistent(ClothesDataComponent.CODEC)
			.networkSynchronized(ClothesDataComponent.STREAM_CODEC)
			.cacheEncoding());
}