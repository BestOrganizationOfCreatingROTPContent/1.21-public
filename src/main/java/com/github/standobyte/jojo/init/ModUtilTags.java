package com.github.standobyte.jojo.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModUtilTags {

	public static class Blocks {
		// this shit fucking sucks, why were block materials removed
		public static final TagKey<Block> CRAZY_D_CAN_MAKE_BULLET = TagKey.create(Registries.BLOCK, 
				ResourceLocation.fromNamespaceAndPath("jojo_ripples", "crazy_d_can_make_bullet"));
	}

}
