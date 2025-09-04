package com.github.standobyte.jojo.util;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientProxy;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
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
	


    protected static float lerpRotation(float currentRotation, float targetRotation) {
        while (targetRotation - currentRotation < -180.0F) {
            currentRotation -= 360.0F;
        }

        while (targetRotation - currentRotation >= 180.0F) {
            currentRotation += 360.0F;
        }

        return Mth.lerp(0.2F, currentRotation, targetRotation);
    }
	
	public static void wrapYRotationAngles(LivingEntity entity) {
		float yRot = entity.getYRot();
		float diff = 0;
		while (yRot < -180) { yRot += 360; diff += 360; }
		while (yRot >= 180) { yRot -= 360; diff -= 360; }
		if (diff != 0) {
			entity.setYRot(yRot);
			entity.yRotO += diff;
			if (entity.level().isClientSide()) {
				wrapClientSide(entity, diff);
			}
		}

		yRot = entity.yBodyRot;
		diff = 0;
		while (yRot < -180) { yRot += 360; diff += 360; }
		while (yRot >= 180) { yRot -= 360; diff -= 360; }
		if (diff != 0) {
			entity.yBodyRot = yRot;
			entity.yBodyRotO += diff;
		}

		yRot = entity.yHeadRot;
		diff = 0;
		while (yRot < -180) { yRot += 360; diff += 360; }
		while (yRot >= 180) { yRot -= 360; diff -= 360; }
		if (diff != 0) {
			entity.yHeadRot = yRot;
			entity.yHeadRotO += diff;
		}
	}
	
	private static void wrapClientSide(LivingEntity entity, float diff) {
		if (entity instanceof LocalPlayer needsToBeInAnotherMethod) {
			needsToBeInAnotherMethod.yBob += diff;
			needsToBeInAnotherMethod.yBobO += diff;
		}
	}
	
}
