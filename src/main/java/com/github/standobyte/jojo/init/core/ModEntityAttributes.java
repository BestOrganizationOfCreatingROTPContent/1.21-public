package com.github.standobyte.jojo.init.core;

import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntityAttributes {
	public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, JojoMod.MOD_ID);

	
	public static final Holder<Attribute> STAND_EFFECTIVE_RANGE = ATTRIBUTES.register("stand_effective_range", 
			() -> new RangedAttribute("attribute.name.jojo_ripples.stand_effective_range", 4, 0, Double.MAX_VALUE).setSyncable(true));
	
	public static final Holder<Attribute> STAND_MAX_RANGE = ATTRIBUTES.register("stand_max_range", 
			() -> new RangedAttribute("attribute.name.jojo_ripples.stand_max_range", 2, 0, Double.MAX_VALUE).setSyncable(true));
	
	public static final Holder<Attribute> STAND_DURABILITY = ATTRIBUTES.register("stand_durability", 
			() -> new RangedAttribute("attribute.name.jojo_ripples.stand_durability", 0, 0, 1024).setSyncable(true));
	
	public static final Holder<Attribute> STAND_PRECISION = ATTRIBUTES.register("stand_precision", 
			() -> new RangedAttribute("attribute.name.jojo_ripples.stand_precision", 0, 0, 160).setSyncable(true));
}
