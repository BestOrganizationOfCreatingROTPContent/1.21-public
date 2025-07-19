package com.github.standobyte.jojo.util;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientProxy;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class UtilFunctions {

	public static Iterable<Entity> getEntities(Level level) {
		if (level instanceof ServerLevel serverLevel) {
			return serverLevel.getAllEntities();
		}
		if (level.isClientSide()) {
			return ClientProxy.getEntities(level);
		}
		throw new IllegalArgumentException();
	}
	
	@Nullable
	public static ResourceLocation getItemId(ItemStack item) {
		Holder<Item> holder = item.getItemHolder();
        return holder.unwrapKey().map(key -> key.location()).orElse(null);
	}

	public static HumanoidArm getHandSide(LivingEntity entity, InteractionHand hand) {
		return hand == InteractionHand.MAIN_HAND ? entity.getMainArm() : entity.getMainArm().getOpposite();
	}

	public static InteractionHand getHand(LivingEntity entity, HumanoidArm handSide) {
		return entity.getMainArm() == handSide ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
	}
	
}
