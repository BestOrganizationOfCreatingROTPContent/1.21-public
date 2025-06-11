package com.github.standobyte.jojo.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.github.standobyte.jojo.util.damage.RipplesModifiedDamageSource;

import net.minecraft.world.damagesource.DamageSource;

@Mixin(DamageSource.class)
public class DamageSourceMixin implements RipplesModifiedDamageSource {
	private float jojo_ripples$addKnockback = 0;
	private float jojo_ripples$knockbackMultiplier = 1;
	
	@Override
	public void jojo_ripples$modifyKnockback(float add, float multiply) {
		this.jojo_ripples$addKnockback = add;
		this.jojo_ripples$knockbackMultiplier = multiply;
	}
	
	@Override
	public float jojo_ripples$knockbackMultiplier() {
		return jojo_ripples$knockbackMultiplier;
	}
	
	@Override
	public float jojo_ripples$addKnockback() {
		return jojo_ripples$addKnockback;
	}
}
