package com.github.standobyte.jojo.util.damage;

import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public interface RipplesModifiedDamageSource {
	void jojo_ripples$modifyKnockback(float add, float multiply);
	float jojo_ripples$knockbackMultiplier();
	float jojo_ripples$addKnockback();
	
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void _onKnockbackEvent(LivingKnockBackEvent event) {
		LivingEntity target = event.getEntity();
		if (!target.damageContainers.isEmpty()) {
			DamageContainer curDamage = target.damageContainers.peek();
			DamageSource dmgSource = curDamage.getSource();
			if (dmgSource instanceof RipplesModifiedDamageSource kbModifier) {
				event.setStrength((event.getStrength() + kbModifier.jojo_ripples$addKnockback()) * kbModifier.jojo_ripples$knockbackMultiplier());
			}
		}
	}
}
