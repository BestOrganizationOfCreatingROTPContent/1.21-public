package com.github.standobyte.jojo.mechanics.grab;

import java.util.Optional;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.util.MathUtil;
import com.github.standobyte.jojo.util.UtilFunctions;
import com.github.standobyte.jojo.util.entitycomponent.TickingEntityData;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class LivingComponentGrab implements TickingEntityData {
	static final AttributeModifier GRABBED_NO_ATTACK_POWER = new AttributeModifier(
			JojoMod.resLoc("grabbed_no_attack"), -1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
	static final AttributeModifier GRABBED_NO_GRAVITY = new AttributeModifier(
			JojoMod.resLoc("grabbed_no_gravity"), -1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
	
	private final LivingEntity thisEntity;
	private LivingEntity grabbingEntity = null;
	private LivingEntity grabbedTarget = null;
	
	public LivingComponentGrab(LivingEntity entity) {
		this.thisEntity = entity;
		addTicking(entity);
	}
	
	
	@Nullable
	public static LivingEntity getEntityGrabbedBy(LivingEntity grabbing) {
		if (!grabbing.hasData(ModDataAttachmentTypes.LIVING_GRAB.get())) return null;;
		
		LivingComponentGrab grabbing_ = grabbing.getData(ModDataAttachmentTypes.LIVING_GRAB.get());
		return grabbing_.grabbedTarget;
	}
	
	@Nullable
	public static LivingEntity getEntityGrabbing(LivingEntity target) {
		if (!target.hasData(ModDataAttachmentTypes.LIVING_GRAB.get())) return null;;
		
		LivingComponentGrab target_ = target.getData(ModDataAttachmentTypes.LIVING_GRAB.get());
		return target_.grabbingEntity;
	}

	
	@Override
	public void tick() {
		tickGrabbedEntity();
		tickBeingGrabbed();
	}
	
	public void setGrabTarget(LivingEntity target) {
		if (grabbedTarget != null && grabbedTarget != target) {
			grabbedTarget
			.getData(ModDataAttachmentTypes.LIVING_GRAB.get())
			.setGrabbedBy(null);
		}

		if (target != null) {
			LivingComponentGrab target_ = target.getData(ModDataAttachmentTypes.LIVING_GRAB.get());
			boolean canBeGrabbed = !target_.isGrabbed();
			if (canBeGrabbed) {
				target_.setGrabbedBy(thisEntity);
			}
		}

		grabbedTarget = target;
		if (!thisEntity.level().isClientSide()) {
			PacketDistributor.sendToPlayersTrackingEntityAndSelf(thisEntity, new TrSetGrabbedEntityPacket(thisEntity.getId(), target != null ? target.getId() : -1));
		}
	}
	
	public boolean isGrabbed() {
		return grabbingEntity != null && grabbingEntity.isAlive();
	}

	public LivingEntity getGrabbedEntity() {
		return grabbedTarget;
	}
	
	@ApiStatus.Internal
	public void setGrabbedBy(LivingEntity grabbing) {
		if (!thisEntity.level().isClientSide()) {
			Optional.ofNullable(thisEntity.getAttribute(Attributes.ATTACK_DAMAGE)).ifPresent(attackDamage -> {
				if (grabbing != null)	attackDamage.addTransientModifier(GRABBED_NO_ATTACK_POWER);
				else					attackDamage.removeModifier(GRABBED_NO_ATTACK_POWER);
			});
			Optional.ofNullable(thisEntity.getAttribute(Attributes.GRAVITY)).ifPresent(gravity -> {
				if (grabbing != null)	gravity.addTransientModifier(GRABBED_NO_GRAVITY);
				else					gravity.removeModifier(GRABBED_NO_GRAVITY);
			});

			if (thisEntity.isPassenger()) {
				thisEntity.stopRiding();
			}
		}

		this.grabbingEntity = grabbing;
	}
	
	
	private void tickBeingGrabbed() {
		if (grabbingEntity != null) {
			if (!grabbingEntity.isAlive()) {
				grabbingEntity
				.getData(ModDataAttachmentTypes.LIVING_GRAB.get())
				.setGrabbedBy(null);
			}
			else {
				thisEntity.fallDistance = 0;
			}
		}
	}
	
	private void tickGrabbedEntity() {
		if (grabbedTarget != null) {
			if (!grabbedTarget.isAlive()) {
				setGrabTarget(null);
			}
		}
	}

	public void setGrabbedPos() {
		if (grabbingEntity != null) {
			Vec3 grabbedPos = grabbingEntity.position()
					.add(new Vec3(0.25, 0, 1)
							.xRot(-grabbingEntity.getXRot() * MathUtil.DEG_TO_RAD)
							.yRot(-grabbingEntity.yBodyRot * MathUtil.DEG_TO_RAD)
							.add(0, grabbingEntity.getEyeHeight() - thisEntity.getEyeHeight(), 0));
			thisEntity.setPos(grabbedPos.x, grabbedPos.y, grabbedPos.z);
			thisEntity.setDeltaMovement(Vec3.ZERO);
			for (Entity passenger : thisEntity.getPassengers()) {
				thisEntity.positionRider(passenger);
			}
		}
	}
	
	@SubscribeEvent
	public static void onLevelTickPost(LevelTickEvent.Post event) {
		Level level = event.getLevel();
		for (Entity entity : UtilFunctions.getEntities(level)) {
			if (entity != null && entity.hasData(ModDataAttachmentTypes.LIVING_GRAB.get())) {
				entity.getData(ModDataAttachmentTypes.LIVING_GRAB.get()).setGrabbedPos();
			}
		}
	}
	
}
