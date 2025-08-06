package com.github.standobyte.v1_21_4_stuff.missingmethods;

import java.util.function.UnaryOperator;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;

// weird that i'm trying to backport not one, but two cool rendering features from 1.21.2 just because they're convenient to use
public class __ItemModelComponent {

	public static UnaryOperator<DataComponentType.Builder<ResourceLocation>> builder() {
		return builder -> builder
				.persistent(ResourceLocation.CODEC)
				.networkSynchronized(ResourceLocation.STREAM_CODEC)
				.cacheEncoding();
	}
}
