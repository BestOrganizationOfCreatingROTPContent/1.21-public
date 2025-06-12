package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

public class ModDamageTypes {
	public static final ResourceKey<DamageType> STAND_ATTACK = ResourceKey.create(Registries.DAMAGE_TYPE, JojoMod.resLoc("stand_attack"));
	
	public static final TagKey<DamageType> CAN_HURT_STANDS = TagKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("jojo", "can_hurt_stands"));
}
