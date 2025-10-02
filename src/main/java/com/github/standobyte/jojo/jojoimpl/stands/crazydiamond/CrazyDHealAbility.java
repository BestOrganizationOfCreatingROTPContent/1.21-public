package com.github.standobyte.jojo.jojoimpl.stands.crazydiamond;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class CrazyDHealAbility {

	public static double crazyDRestorationSpeed(StandEntity standEntity) {
		return standEntity.getAttackSpeed() * 0.05F + 0.55;
	}

	public static void addParticlesAround(Entity entity) {
		Level level = entity.level();
		if (level.isClientSide() && ClientGlobals.canSeeStands) {
			int particlesCount = Math.max(Mth.ceil(entity.getBbWidth() * (entity.getBbHeight() * 2 * entity.getBbHeight())), 1);
			for (int i = 0; i < particlesCount; i++) {
				level.addParticle(ModParticles.CD_RESTORATION.get(), entity.getRandomX(1), entity.getRandomY(), entity.getRandomZ(1), 0, 0, 0);
			}
		}
	}

}
