package com.github.standobyte.jojo.init;

import java.util.function.Supplier;

import com.github.standobyte.jojo.mc.item.component.StandWrittenOnDisc;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItemDataComponents {
	public static final DeferredRegister.DataComponents DATA_COMPONENT_TYPES = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, "jojo_rotp");
	
	public static final Supplier<DataComponentType<StandWrittenOnDisc>> DISC_STAND = DATA_COMPONENT_TYPES.registerComponentType("disc_stand", 
			builder -> builder
			.persistent(StandWrittenOnDisc.CODEC)
			.networkSynchronized(StandWrittenOnDisc.STREAM_CODEC)
			.cacheEncoding());
}