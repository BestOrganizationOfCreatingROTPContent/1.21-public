package com.github.standobyte.jojo.client.input;

import com.github.standobyte.jojo.core.packet.fromclient.ClAimTargetPacket;
import com.github.standobyte.jojo.jojoimpl.stands._entitybase.StandEntityPunchAbility;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.util.StandUtil;
import com.github.standobyte.jojo.util.target.ActionTarget;
import com.github.standobyte.jojo.util.target.ActionTargetAim;
import com.github.standobyte.jojo.util.target.HitResultUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;

public class ClientsideAim {
	public static ActionTarget cameraEntityAimTarget = ActionTarget.EMPTY;
	public static ActionTargetAim playerAim = new ActionTargetAim();
	public static ActionTargetAim standAim = new ActionTargetAim();
	
	public static void updateTarget(Minecraft mc, float partialTick) {
		cameraEntityAimTarget = mc.hitResult != null ? ActionTarget.fromVanilla(mc.hitResult) : ActionTarget.EMPTY;

		if (mc.level != null && mc.player != null) {
			boolean isPlayerCameraEntity = mc.player == mc.cameraEntity || mc.cameraEntity == null;
			if (isPlayerCameraEntity) {
				playerAim.setTarget(cameraEntityAimTarget);
			}
			else {
				playerAim.setTarget(ActionTarget.EMPTY);
			}
			
			StandEntity stand = StandUtil.getSummonedStand(mc.player);
			if (stand != null) {
				EntityActionInstance curAction = LivingComponentAction.getCurEntityAction(stand);

				LivingEntity aimingEntity;
				if (isPlayerCameraEntity && curAction == null) {
					aimingEntity = mc.player;
				}
				else if (curAction != null) {
					aimingEntity = switch (curAction.aimAs) {
						case PLAYER -> mc.player;
						case STAND -> stand;
					};
				}
				else {
					aimingEntity = stand;
				}
				ActionTarget target = ActionTarget.fromVanilla(HitResultUtil.clipEntityLook(
						// TODO stand aiming for other abilities that do not need friendly fire check (e.g. healing)
						aimingEntity, entity -> StandEntityPunchAbility.canStandHit(stand, entity)));
				standAim.setTarget(target);
			}
			else {
				standAim.setTarget(ActionTarget.EMPTY);
			}
		}
		else {
			playerAim.setTarget(ActionTarget.EMPTY);
			standAim.setTarget(ActionTarget.EMPTY);
		}
	}
	
	public static void updateTargetWithServer(Minecraft mc) {
		if (mc.level != null) {
			if (playerAim.checkDirty()) {
				PacketDistributor.sendToServer(new ClAimTargetPacket(playerAim.getTarget(), ClAimTargetPacket.PacketType.PLAYER));
			}
			
			if (standAim.checkDirty()) {
				PacketDistributor.sendToServer(new ClAimTargetPacket(playerAim.getTarget(), ClAimTargetPacket.PacketType.STAND));
			}
		}
	}

}
