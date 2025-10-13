package com.github.standobyte.jojo.mechanics.entitycontrol;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.util.entitycomponent.ComponentUtil;
import com.github.standobyte.jojo.util.entitycomponent.TickingEntityData;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;

public class ServerEntityController implements TickingEntityData {
	public final Entity thisEntity;
	@Nullable private LivingEntity controllingEntity;
	public String controllerType;
	@Nullable private Entity controlTarget;
	
	public ServerEntityController(Entity entity) {
		this.thisEntity = entity;
		addTicking(entity);
	}
	
	public void stopControlling() {
		setControlTarget(null, null);
	}
	
	public void setControlTarget(@Nullable Entity controlTarget, @Nullable String clientControllerType) {
		if (thisEntity instanceof LivingEntity controllingEntity) {
			if (this.controlTarget != null) {
				ServerEntityController prevTargetComponent = this.controlTarget.getData(ModDataAttachmentTypes.CONTROLLER.get());
				prevTargetComponent.controllingEntity = null;
				prevTargetComponent.controllerType = null;
			}
			
			if (controlTarget != null) {
				ServerEntityController newTargetComponent = controlTarget.getData(ModDataAttachmentTypes.CONTROLLER.get());
				newTargetComponent.controllingEntity = controllingEntity;
				newTargetComponent.controllerType = clientControllerType;
			}
			
			if (clientControllerType != null && controllingEntity instanceof ServerPlayer player) {
				PacketDistributor.sendToPlayer(player, new SetClientControllerPacket(controlTarget != null ? controlTarget.getId() : -1, clientControllerType));
			}
			
			this.controlTarget = controlTarget;
			this.controllerType = clientControllerType;
		}
	}
	
	public static void setServerControlTarget(LivingEntity controllingEntity, @Nullable Entity targetEntity, @Nullable String setOnClientType) {
		if (targetEntity != null) {
			ServerEntityController component = controllingEntity.getData(ModDataAttachmentTypes.CONTROLLER.get());
			component.setControlTarget(targetEntity, setOnClientType);
		}
		else {
			ServerEntityController component = ComponentUtil.getExistingDataOrNull(controllingEntity, ModDataAttachmentTypes.CONTROLLER);
			if (component != null) {
				component.stopControlling();
			}
		}
	}
	
	
	@Nullable
	public static Entity getControlTarget(LivingEntity controllerEntity) {
		ServerEntityController component = ComponentUtil.getExistingDataOrNull(controllerEntity, ModDataAttachmentTypes.CONTROLLER);
		return component != null ? component.controlTarget : null;
	}
	
	@Nullable
	public static LivingEntity getControllerEntity(Entity targetEntity) {
		ServerEntityController component = ComponentUtil.getExistingDataOrNull(targetEntity, ModDataAttachmentTypes.CONTROLLER);
		return component != null ? component.controllingEntity : null;
	}
		

	@Override
	public void tick() {
		if (controllingEntity != null && !controllingEntity.isAlive()) {
			ServerEntityController controllerComponent = controllingEntity.getData(ModDataAttachmentTypes.CONTROLLER);
			controllerComponent.stopControlling();
		}
	}
	
	@Nullable
	public LivingEntity getControllingEntity() {
		return controllingEntity;
	}
	
	@Nullable
	public Entity getControlTarget() {
		return controlTarget;
	}

}
